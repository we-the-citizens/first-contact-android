package ro.wethecitizens.firstcontact.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import ro.wethecitizens.firstcontact.BuildConfig
import ro.wethecitizens.firstcontact.MainActivity
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.bluetooth.gatt.ACTION_RECEIVED_STATUS
import ro.wethecitizens.firstcontact.bluetooth.gatt.ACTION_RECEIVED_STREETPASS
import ro.wethecitizens.firstcontact.bluetooth.gatt.STATUS
import ro.wethecitizens.firstcontact.bluetooth.gatt.STREET_PASS
import ro.wethecitizens.firstcontact.idmanager.TempIDManager
import ro.wethecitizens.firstcontact.idmanager.TemporaryID
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.notifications.NotificationTemplates
import ro.wethecitizens.firstcontact.permissions.RequestFileWritePermission
import ro.wethecitizens.firstcontact.status.Status
import ro.wethecitizens.firstcontact.status.persistence.StatusRecord
import ro.wethecitizens.firstcontact.status.persistence.StatusRecordStorage
import ro.wethecitizens.firstcontact.streetpass.ConnectionRecord
import ro.wethecitizens.firstcontact.streetpass.StreetPassScanner
import ro.wethecitizens.firstcontact.streetpass.StreetPassServer
import ro.wethecitizens.firstcontact.streetpass.StreetPassWorker
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordStorage
import ro.wethecitizens.firstcontact.temp_id_db.TempId
import ro.wethecitizens.firstcontact.temp_id_db.TempIdStorage
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class BluetoothMonitoringService : Service(), CoroutineScope {

    private var mNotificationManager: NotificationManager? = null

    private lateinit var serviceUUID: String

    private var streetPassServer: StreetPassServer? = null
    private var streetPassScanner: StreetPassScanner? = null
    private var advertiser:ro.wethecitizens.firstcontact.bluetooth.BLEAdvertiser? = null

    var worker: StreetPassWorker? = null

    private val streetPassReceiver = StreetPassReceiver()
    private val statusReceiver = StatusReceiver()
    private val bluetoothStatusReceiver = BluetoothStatusReceiver()

    private lateinit var streetPassRecordStorage: StreetPassRecordStorage
    private lateinit var statusRecordStorage: StatusRecordStorage
    private lateinit var tempIdStorage : TempIdStorage

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var commandHandler: CommandHandler

    private lateinit var localBroadcastManager: LocalBroadcastManager

    private var notificationShown: NOTIFICATION_STATE? = null


    fun alertaInfectare(){
        var intent = Intent(this.applicationContext, MainActivity::class.java)

        val activityPendingIntent = PendingIntent.getActivity(
            this.applicationContext, PENDING_ACTIVITY,
            intent, 0
        )
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alerta de infectare posibila COVID19")
            .setSmallIcon(R.drawable.ic_notification_service)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Auto-izolativa, evitati contactul cu alte persoane, anuntati posibilitatea infectarii la medicul de familie si la DSU in vederea programarii pentru testare."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 1000, 1000, 1000, 1000))
            .setLights(Color.RED, 3000, 3000)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(activityPendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "nume"
            val descriptionText = "descriere"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
//        createNotificationChannel()
//        alertaInfectare()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        setup()
    }

    fun setup() {

        val fetch = TempIDManager.retrieveNewTemporaryID(this.applicationContext)
        fetch?.let {
            broadcastMessage = it
            CentralLog.i(TAG, "Setup TemporaryID to ${it.tempID}")
        }


        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        CentralLog.setPowerManager(pm)

        commandHandler = CommandHandler(WeakReference(this))

        CentralLog.d(TAG, "Creating service - BluetoothMonitoringService")
        serviceUUID = BuildConfig.BLE_SSID

        worker = StreetPassWorker(this.applicationContext)

        unregisterReceivers()
        registerReceivers()

        streetPassRecordStorage = StreetPassRecordStorage(this.applicationContext)
        statusRecordStorage = StatusRecordStorage(this.applicationContext)
        tempIdStorage = TempIdStorage(this.applicationContext)

        setupNotifications()
    }

    fun teardown() {
        streetPassServer?.tearDown()
        streetPassServer = null

        streetPassScanner?.stopScan()
        streetPassScanner = null

        commandHandler.removeCallbacksAndMessages(null)

        ro.wethecitizens.firstcontact.Utils.cancelBMUpdateCheck(this.applicationContext)
        ro.wethecitizens.firstcontact.Utils.cancelNextScan(this.applicationContext)
        ro.wethecitizens.firstcontact.Utils.cancelNextAdvertise(this.applicationContext)
    }

    private fun setupNotifications() {

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_SERVICE
            // Create the channel for the notification
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
            mChannel.enableLights(false)
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(0L)
            mChannel.setSound(null, null)
            mChannel.setShowBadge(false)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager!!.createNotificationChannel(mChannel)
        }
    }

    private fun notifyLackingThings(override: Boolean = false) {
        if (notificationShown != NOTIFICATION_STATE.LACKING_THINGS || override) {
            var notif =
                NotificationTemplates.lackingThingsNotification(this.applicationContext, CHANNEL_ID)
            startForeground(NOTIFICATION_ID, notif)
            notificationShown = NOTIFICATION_STATE.LACKING_THINGS
        }
    }

    private fun notifyRunning(override: Boolean = false) {
        if (notificationShown != NOTIFICATION_STATE.RUNNING || override) {
            var notif =
                NotificationTemplates.getRunningNotification(this.applicationContext, CHANNEL_ID)
            startForeground(NOTIFICATION_ID, notif)
            notificationShown = NOTIFICATION_STATE.RUNNING
        }
    }

    private fun hasLocationPermissions(): Boolean {
        val perms = ro.wethecitizens.firstcontact.Utils.getRequiredPermissions()
        return EasyPermissions.hasPermissions(this.applicationContext, *perms)
    }

    private fun hasWritePermissions(): Boolean {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return EasyPermissions.hasPermissions(this.applicationContext, *perms)
    }

    private fun acquireWritePermission() {
        val intent = Intent(this.applicationContext, RequestFileWritePermission::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun isBluetoothEnabled(): Boolean {
        var btOn = false
        val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter
        }

        bluetoothAdapter?.let {
            btOn = it.isEnabled
        }
        return btOn
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        CentralLog.i(TAG, "")
        CentralLog.i(TAG, "----------------------")
        CentralLog.i(TAG, "onStartCommand")

        //check for permissions
        if (!hasLocationPermissions() || !isBluetoothEnabled()) {
            CentralLog.i(
                TAG,
                "location permission: ${hasLocationPermissions()} bluetooth: ${isBluetoothEnabled()}"
            )
            notifyLackingThings()
            return START_STICKY
        }

        //check for write permissions  - not required for now. SDLog maybe?
        //only required for debug builds - for now
        if (BuildConfig.DEBUG) {
            if (!hasWritePermissions()) {
                CentralLog.i(TAG, "no write permission")
                //start write permission activity
                acquireWritePermission()
                stopSelf()
                return START_STICKY
            }
        }

        intent?.let {
            val cmd = intent.getIntExtra(COMMAND_KEY, Command.INVALID.index)
            runService(Command.findByValue(cmd))

            return START_STICKY
        }

        if (intent == null) {
            CentralLog.e(TAG, "WTF? Nothing in intent @ onStartCommand")
//            Utils.startBluetoothMonitoringService(applicationContext)
            commandHandler.startBluetoothMonitoringService()
        }

        // Tells the system to not try to recreate the service after it has been killed.
        return START_STICKY
    }

    fun runService(cmd: Command?) {

        var doWork = true

        CentralLog.i(TAG, "")
        CentralLog.i(TAG, "-------------------------------------------------------------------------")
        CentralLog.i(TAG, "runService -> Command is: ${cmd?.string}")

        //check for permissions
        if (!hasLocationPermissions() || !isBluetoothEnabled()) {
            CentralLog.i(
                TAG,
                "location permission: ${hasLocationPermissions()} bluetooth: ${isBluetoothEnabled()}"
            )
            notifyLackingThings()
            return
        }

        //check for write permissions  - not required for now. SDLog maybe?
        //only required for debug builds - for now
        if (BuildConfig.DEBUG) {
            if (!hasWritePermissions()) {
                CentralLog.i(TAG, "no write permission")
                //start write permission activity
                acquireWritePermission()
                stopSelf()
                return
            }
        }

        //show running foreground notification if its not showing that
        notifyRunning()

        when (cmd) {
            Command.ACTION_START -> {
                setupService()
                ro.wethecitizens.firstcontact.Utils.scheduleNextHealthCheck(this.applicationContext, healthCheckInterval)
                ro.wethecitizens.firstcontact.Utils.scheduleRepeatingPurge(this.applicationContext, purgeInterval)
                ro.wethecitizens.firstcontact.Utils.scheduleBMUpdateCheck(this.applicationContext, bmCheckInterval)
                actionStart()
            }

            Command.ACTION_SCAN -> {
                scheduleScan()

                if (doWork) {
                    actionScan()
                }
            }

            Command.ACTION_ADVERTISE -> {
                scheduleAdvertisement()
                if (doWork) {
                    actionAdvertise()
                }

                saveTempID();
            }

            Command.ACTION_UPDATE_BM -> {
                ro.wethecitizens.firstcontact.Utils.scheduleBMUpdateCheck(this.applicationContext, bmCheckInterval)
                actionUpdateBm()
            }

            Command.ACTION_STOP -> {
                actionStop()
            }

            Command.ACTION_SELF_CHECK -> {
                ro.wethecitizens.firstcontact.Utils.scheduleNextHealthCheck(this.applicationContext, healthCheckInterval)
                if (doWork) {
                    actionHealthCheck()
                }
            }

            Command.ACTION_PURGE -> {
                actionPurge()
            }

            else -> CentralLog.i(TAG, "Invalid / ignored command: $cmd. Nothing to do")
        }
    }

    private fun actionStop() {
        stopForeground(true)
        stopSelf()
        CentralLog.w(TAG, "Service Stopping")
    }

    private fun actionHealthCheck() {
        performHealthCheck()
        ro.wethecitizens.firstcontact.Utils.scheduleRepeatingPurge(this.applicationContext, purgeInterval)
    }

    private fun actionPurge() {
        performPurge()
    }

    private fun actionStart() {

        CentralLog.d(TAG, "Action Start")

        setupCycles()

//        saveGUID()
//        updateTempID()
    }

    fun actionUpdateBm() {

        if (TempIDManager.needToUpdate(this.applicationContext) || broadcastMessage == null) {

            CentralLog.i(TAG, "Need to update TemporaryID in actionUpdateBM")

            val fetch = TempIDManager.retrieveNewTemporaryID(this.applicationContext)
            fetch?.let {

                broadcastMessage = it

                CentralLog.i(TAG, "Update TemporaryID to ${it.tempID}")
            }

            if (fetch == null) {
                CentralLog.e(TAG, "Failed to fetch new Temp ID")
            }
        }
//        else {
//            CentralLog.i(TAG, "[TempID] Don't need to update Temp ID in actionUpdateBM")
//        }
    }

    fun calcPhaseShift(min: Long, max: Long): Long {
        return (min + (Math.random() * (max - min))).toLong()
    }

    private fun actionScan() {

        if (TempIDManager.needToUpdate(this.applicationContext) || broadcastMessage == null) {

            CentralLog.i(TAG, "Need to update TemporaryID in actionScan")

            val fetch = TempIDManager.retrieveNewTemporaryID(this.applicationContext)
            fetch?.let {
                broadcastMessage = it

                CentralLog.i(TAG, "Update TemporaryID to ${it.tempID}")

                performScan()
            }
        }
        else {
            //CentralLog.i(TAG, "[TempID] Don't need to update Temp ID in actionScan")
            performScan()
        }
    }

    private fun actionAdvertise() {
        setupAdvertiser()
        if (isBluetoothEnabled()) {
            advertiser?.startAdvertising(advertisingDuration)
        } else {
            CentralLog.w(TAG, "Unable to start advertising, bluetooth is off")
        }
    }

    private fun setupService() {
        streetPassServer =
            streetPassServer ?: StreetPassServer(this.applicationContext, serviceUUID)
        setupScanner()
        setupAdvertiser()
    }

    private fun setupScanner() {
        streetPassScanner = streetPassScanner ?: StreetPassScanner(
            this,
            serviceUUID,
            scanDuration
        )
    }

    private fun setupAdvertiser() {
        advertiser = advertiser ?: ro.wethecitizens.firstcontact.bluetooth.BLEAdvertiser(
            serviceUUID
        )
    }

    private fun setupCycles() {
        setupScanCycles()
        setupAdvertisingCycles()
    }

    private fun setupScanCycles() {
        commandHandler.scheduleNextScan(0)
    }

    private fun setupAdvertisingCycles() {
        commandHandler.scheduleNextAdvertise(0)
    }

    private fun performScan() {
        setupScanner()
        startScan()
    }

    private fun scheduleScan() {
        if (!infiniteScanning) {
            commandHandler.scheduleNextScan(
                scanDuration + calcPhaseShift(
                    minScanInterval,
                    maxScanInterval
                )
            )
        }
    }

    private fun scheduleAdvertisement() {
        if (!infiniteAdvertising) {
            commandHandler.scheduleNextAdvertise(advertisingDuration + advertisingGap)
        }
    }

    private fun startScan() {

        if (isBluetoothEnabled()) {

            streetPassScanner?.let { scanner ->
                if (!scanner.isScanning()) {
                    scanner.startScan()
                } else {
                    CentralLog.e(TAG, "Already scanning!")
                }
            }
        } else {
            CentralLog.w(TAG, "Unable to start scan - bluetooth is off")
        }
    }

    private fun performHealthCheck() {

        CentralLog.i(TAG, "Performing self diagnosis")

        if (!hasLocationPermissions() || !isBluetoothEnabled()) {
            CentralLog.i(TAG, "no location permission")
            notifyLackingThings(true)
            return
        }

        notifyRunning(true)

        //ensure our service is there
        setupService()

        if (!infiniteScanning) {
            if (!commandHandler.hasScanScheduled()) {
                CentralLog.w(TAG, "Missing Scan Schedule - rectifying")
                commandHandler.scheduleNextScan(100)
            } else {
                CentralLog.w(TAG, "Scan Schedule present")
            }
        } else {
            CentralLog.w(TAG, "Should be operating under infinite scan mode")
        }

        if (!infiniteAdvertising) {
            if (!commandHandler.hasAdvertiseScheduled()) {
                CentralLog.w(TAG, "Missing Advertise Schedule - rectifying")
//                setupAdvertisingCycles()
                commandHandler.scheduleNextAdvertise(100)
            } else {
                CentralLog.w(
                    TAG,
                    "Advertise Schedule present. Should be advertising?:  ${advertiser?.shouldBeAdvertising
                        ?: false}. Is Advertising?: ${advertiser?.isAdvertising ?: false}"
                )
            }
        } else {
            CentralLog.w(TAG, "Should be operating under infinite advertise mode")
        }


    }

    private fun performPurge() {
        val context = this
        launch {
            val before = System.currentTimeMillis() - purgeTTL
            CentralLog.i(TAG, "Coroutine - Purging of data before epoch time $before")

            streetPassRecordStorage.purgeOldRecords(before)
            statusRecordStorage.purgeOldRecords(before)
            tempIdStorage.purgeOldRecords(before)

            ro.wethecitizens.firstcontact.Preference.putLastPurgeTime(context, System.currentTimeMillis())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CentralLog.i(TAG, "BluetoothMonitoringService destroyed - tearing down")
        stopService()
        CentralLog.i(TAG, "BluetoothMonitoringService destroyed")
    }

    private fun stopService() {
        teardown()
        unregisterReceivers()

        worker?.terminateConnections()
        worker?.unregisterReceivers()

        job.cancel()
    }

    private fun saveTempID() {

        if (!TempIDManager.needToBeSaved())
            return

        CentralLog.i(TAG, "TempID save new record start")

        val appCtx = this.applicationContext;
        val cti = TempIDManager.retrieveCurrentTemporaryID(appCtx)


        launch {

            tempIdStorage.saveRecord(TempId(v = cti.tempID))

            TempIDManager.markAsSaved()

            CentralLog.i(TAG, "TempID = ${cti.tempID}")
            CentralLog.i(TAG, "TempID save done")



            CentralLog.i(TAG, "TempID last 10 records")

            for (t in tempIdStorage.getLast10Records().asReversed()) {

                CentralLog.i(TAG, "TempID = ${t.v}  ts = ${t.timestamp}")
            }


            //Se foloseste doar in dev cand sunt prea multe in lista de mai sus
            //tempIdStorage.nukeDb()
        }
    }

    private fun registerReceivers() {
        val recordAvailableFilter = IntentFilter(ACTION_RECEIVED_STREETPASS)
        localBroadcastManager.registerReceiver(streetPassReceiver, recordAvailableFilter)

        val statusReceivedFilter = IntentFilter(ACTION_RECEIVED_STATUS)
        localBroadcastManager.registerReceiver(statusReceiver, statusReceivedFilter)

        val bluetoothStatusReceivedFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStatusReceiver, bluetoothStatusReceivedFilter)

        CentralLog.i(TAG, "Receivers registered")
    }

    private fun unregisterReceivers() {
        try {
            localBroadcastManager.unregisterReceiver(streetPassReceiver)
        } catch (e: Throwable) {
            CentralLog.w(TAG, "streetPassReceiver is not registered?")
        }

        try {
            localBroadcastManager.unregisterReceiver(statusReceiver)
        } catch (e: Throwable) {
            CentralLog.w(TAG, "statusReceiver is not registered?")
        }

        try {
            unregisterReceiver(bluetoothStatusReceiver)
        } catch (e: Throwable) {
            CentralLog.w(TAG, "bluetoothStatusReceiver is not registered?")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    inner class BluetoothStatusReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val action = intent.action
                if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    var state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)

                    when (state) {
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            CentralLog.d(TAG, "BluetoothAdapter.STATE_TURNING_OFF")
                            notifyLackingThings()
                            teardown()
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            CentralLog.d(TAG, "BluetoothAdapter.STATE_OFF")
                        }
                        BluetoothAdapter.STATE_TURNING_ON -> {
                            CentralLog.d(TAG, "BluetoothAdapter.STATE_TURNING_ON")
                        }
                        BluetoothAdapter.STATE_ON -> {
                            CentralLog.d(TAG, "BluetoothAdapter.STATE_ON")
                            ro.wethecitizens.firstcontact.Utils.startBluetoothMonitoringService(this@BluetoothMonitoringService.applicationContext)
                        }
                    }
                }
            }
        }
    }

    inner class StreetPassReceiver : BroadcastReceiver() {

        private val TAG = "StreetPassReceiver"

        override fun onReceive(context: Context, intent: Intent) {

            if (ACTION_RECEIVED_STREETPASS == intent.action) {
                var connRecord: ConnectionRecord = intent.getParcelableExtra(STREET_PASS)
                CentralLog.d(
                    TAG,
                    "StreetPass received: $connRecord"
                )

                if (connRecord.msg.isNotEmpty()) {
                    val record = StreetPassRecord(
                        v = connRecord.version,
                        msg = connRecord.msg,
                        org = connRecord.org,
                        modelP = connRecord.peripheral.modelP,
                        modelC = connRecord.central.modelC,
                        rssi = connRecord.rssi,
                        txPower = connRecord.txPower
                    )

                    launch {

                        streetPassRecordStorage.saveRecord(record)

                        CentralLog.d(TAG,
                            "StreetPassRecord save done " +
                                    "msg = ${record.msg} " +
                                    "ts = ${ro.wethecitizens.firstcontact.Utils.getDate(record.timestamp)}"
                        )

                        CentralLog.i(TAG, "StreetPassRecord last 10 records")

                        for (t in streetPassRecordStorage.getLast10Records().asReversed()) {

                            CentralLog.i(TAG, "StreetPassRecord   msg = ${t.msg}  ts = ${t.timestamp}")
                        }
                    }
                }
            }
        }
    }

    inner class StatusReceiver : BroadcastReceiver() {
        private val TAG = "StatusReceiver"

        override fun onReceive(context: Context, intent: Intent) {

            if (ACTION_RECEIVED_STATUS == intent.action) {
                var statusRecord: Status = intent.getParcelableExtra(STATUS)
                CentralLog.d(TAG, "Status received: ${statusRecord.msg}")

                if (statusRecord.msg.isNotEmpty()) {
                    val statusRecord = StatusRecord(statusRecord.msg)
                    launch {
                        statusRecordStorage.saveRecord(statusRecord)
                    }
                }
            }
        }
    }

    enum class Command(val index: Int, val string: String) {
        INVALID(-1, "INVALID"),
        ACTION_START(0, "START"),
        ACTION_SCAN(1, "SCAN"),
        ACTION_STOP(2, "STOP"),
        ACTION_ADVERTISE(3, "ADVERTISE"),
        ACTION_SELF_CHECK(4, "SELF_CHECK"),
        ACTION_UPDATE_BM(5, "UPDATE_BM"),
        ACTION_PURGE(6, "PURGE");

        companion object {
            private val types = values().associate { it.index to it }
            fun findByValue(value: Int) = types[value]
        }
    }

    enum class NOTIFICATION_STATE() {
        RUNNING,
        LACKING_THINGS
    }

    companion object {

        private val TAG = "BTMService"

        private val NOTIFICATION_ID = BuildConfig.SERVICE_FOREGROUND_NOTIFICATION_ID
        private val CHANNEL_ID = BuildConfig.SERVICE_FOREGROUND_CHANNEL_ID
        val CHANNEL_SERVICE = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME

        val PUSH_NOTIFICATION_ID = BuildConfig.PUSH_NOTIFICATION_ID

        val COMMAND_KEY = "${BuildConfig.APPLICATION_ID}_CMD"

        val PENDING_ACTIVITY = 5
        val PENDING_START = 6
        val PENDING_SCAN_REQ_CODE = 7
        val PENDING_ADVERTISE_REQ_CODE = 8
        val PENDING_HEALTH_CHECK_CODE = 9
        val PENDING_WIZARD_REQ_CODE = 10
        val PENDING_BM_UPDATE = 11
        val PENDING_PURGE_CODE = 12

        var broadcastMessage: TemporaryID? = null

        //should be more than advertising gap?
        val scanDuration: Long = BuildConfig.SCAN_DURATION
        val minScanInterval: Long = BuildConfig.MIN_SCAN_INTERVAL
        val maxScanInterval: Long = BuildConfig.MAX_SCAN_INTERVAL

        val advertisingDuration: Long = BuildConfig.ADVERTISING_DURATION
        val advertisingGap: Long = BuildConfig.ADVERTISING_INTERVAL

        val maxQueueTime: Long = BuildConfig.MAX_QUEUE_TIME
        val bmCheckInterval: Long = BuildConfig.BM_CHECK_INTERVAL
        val healthCheckInterval: Long = BuildConfig.HEALTH_CHECK_INTERVAL
        val purgeInterval: Long = BuildConfig.PURGE_INTERVAL
        val purgeTTL: Long = BuildConfig.PURGE_TTL

        val connectionTimeout: Long = BuildConfig.CONNECTION_TIMEOUT

        val blacklistDuration: Long = BuildConfig.BLACKLIST_DURATION

        val infiniteScanning = false
        val infiniteAdvertising = false

        val useBlacklist = true
    }
}
