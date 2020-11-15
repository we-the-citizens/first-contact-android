// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

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
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.*
import pub.devrel.easypermissions.EasyPermissions
import ro.wethecitizens.firstcontact.BuildConfig
import ro.wethecitizens.firstcontact.MainActivity
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.bluetooth.BLEAdvertiser
import ro.wethecitizens.firstcontact.bluetooth.gatt.ACTION_RECEIVED_STATUS
import ro.wethecitizens.firstcontact.bluetooth.gatt.ACTION_RECEIVED_STREETPASS
import ro.wethecitizens.firstcontact.bluetooth.gatt.STATUS
import ro.wethecitizens.firstcontact.bluetooth.gatt.STREET_PASS
import ro.wethecitizens.firstcontact.idmanager.TempIDManager
import ro.wethecitizens.firstcontact.idmanager.TemporaryID
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.notifications.NotificationTemplates
import ro.wethecitizens.firstcontact.permissions.RequestFileWritePermission
import ro.wethecitizens.firstcontact.preference.Preference
import ro.wethecitizens.firstcontact.status.Status
import ro.wethecitizens.firstcontact.status.persistence.StatusRecord
import ro.wethecitizens.firstcontact.status.persistence.StatusRecordStorage
import ro.wethecitizens.firstcontact.streetpass.ConnectionRecord
import ro.wethecitizens.firstcontact.streetpass.StreetPassScanner
import ro.wethecitizens.firstcontact.streetpass.StreetPassServer
import ro.wethecitizens.firstcontact.streetpass.StreetPassWorker
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordStorage
import ro.wethecitizens.firstcontact.idmanager.persistence.TempId
import ro.wethecitizens.firstcontact.idmanager.persistence.TempIdStorage
import ro.wethecitizens.firstcontact.infectionalert.persistence.InfectionAlertRecord
import ro.wethecitizens.firstcontact.infectionalert.persistence.InfectionAlertRecordStorage
import ro.wethecitizens.firstcontact.positivekey.persistence.PositiveKeyRecord
import ro.wethecitizens.firstcontact.positivekey.persistence.PositiveKeyRecordStorage
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.utils.Utils
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.CoroutineContext

class BluetoothMonitoringService : Service(), CoroutineScope {

    private var mNotificationManager: NotificationManager? = null

    private lateinit var serviceUUID: String

    private var streetPassServer: StreetPassServer? = null
    private var streetPassScanner: StreetPassScanner? = null
    private var advertiser:BLEAdvertiser? = null

    var worker: StreetPassWorker? = null

    private val streetPassReceiver = StreetPassReceiver()
    private val statusReceiver = StatusReceiver()
    private val bluetoothStatusReceiver = BluetoothStatusReceiver()

    private lateinit var streetPassRecordStorage: StreetPassRecordStorage
    private lateinit var statusRecordStorage: StatusRecordStorage
    private lateinit var positiveKeysStorage: PositiveKeyRecordStorage
    private lateinit var infectionAlertRecordStorage: InfectionAlertRecordStorage
    private lateinit var tempIdStorage : TempIdStorage

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var commandHandler: CommandHandler
    private lateinit var localBroadcastManager: LocalBroadcastManager

    private var notificationShown: NOTIFICATION_STATE? = null

    override fun onCreate() {
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
        positiveKeysStorage = PositiveKeyRecordStorage(this.applicationContext)
        tempIdStorage =
            TempIdStorage(
                this.applicationContext
            )
        infectionAlertRecordStorage = InfectionAlertRecordStorage(this.applicationContext)

        setupNotifications()
    }

    fun teardown() {
        streetPassServer?.tearDown()
        streetPassServer = null

        streetPassScanner?.stopScan()
        streetPassScanner = null

        commandHandler.removeCallbacksAndMessages(null)

        Utils.cancelBMUpdateCheck(this.applicationContext)
        Utils.cancelNextScan(this.applicationContext)
        Utils.cancelNextAdvertise(this.applicationContext)
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

            val ch2 = NotificationChannel(
                NEW_ALERTS_CHANNEL_ID,
                NEW_ALERTS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            ch2.enableLights(false)
            ch2.enableVibration(true)
            ch2.setShowBadge(false)

            mNotificationManager!!.createNotificationChannel(ch2)


            val ch3 = NotificationChannel(
                OWN_UPLOAD_CHANNEL_ID,
                OWN_UPLOAD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            ch2.enableLights(false)
            ch2.enableVibration(true)
            ch2.setShowBadge(false)

            mNotificationManager!!.createNotificationChannel(ch3)
        }
    }

    private fun notifyLackingThings(override: Boolean = false) {
        CentralLog.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! notifyLackingThings")
        if (notificationShown != NOTIFICATION_STATE.LACKING_THINGS || override) {
            var notif =
                NotificationTemplates.lackingThingsNotification(this.applicationContext, CHANNEL_ID)
            startForeground(NOTIFICATION_ID, notif)
            notificationShown = NOTIFICATION_STATE.LACKING_THINGS
        }
    }

    private fun notifyRunning(override: Boolean = false) {
        CentralLog.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! notifyRunning")
        if (notificationShown != NOTIFICATION_STATE.RUNNING || override) {
            var notif =
                NotificationTemplates.getRunningNotification(this.applicationContext, CHANNEL_ID)
            startForeground(NOTIFICATION_ID, notif)
            notificationShown = NOTIFICATION_STATE.RUNNING
        }
    }

    private fun hasLocationPermissions(): Boolean {
        val perms = Utils.getRequiredPermissions()
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
                Utils.scheduleNextHealthCheck(this.applicationContext, healthCheckInterval)
                Utils.scheduleRepeatingPurge(this.applicationContext, purgeInterval)
                Utils.scheduleBMUpdateCheck(this.applicationContext, bmCheckInterval)

                Utils.schedulePeriodicallyDownloadNextHealthCheck(this.applicationContext, healthCheckInterval)
                Utils.schedulePeriodicallyDownloadRepeatingPurge(this.applicationContext, purgeInterval)
                Utils.schedulePeriodicallyDownloadMatchKeys(this.applicationContext, matchKeysInterval)

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
                Utils.scheduleBMUpdateCheck(this.applicationContext, bmCheckInterval)
                actionUpdateBm()
            }

            Command.ACTION_STOP -> {
                actionStop()
            }

            Command.ACTION_SELF_CHECK -> {
                Utils.scheduleNextHealthCheck(this.applicationContext, healthCheckInterval)
                Utils.schedulePeriodicallyDownloadNextHealthCheck(this.applicationContext, healthCheckInterval)
                if (doWork) {
                    performHealthCheck()
                    Utils.scheduleRepeatingPurge(this.applicationContext, purgeInterval)
                    Utils.schedulePeriodicallyDownloadRepeatingPurge(this.applicationContext, purgeInterval)
                }
            }

            Command.ACTION_PURGE -> {
                actionPurge()
            }

            Command.ACTION_DOWNLOAD -> {
                scheduleDownload()

                if (doWork)
                    performDownload()
            }

            Command.ACTION_MATCH_KEYS -> {
                Utils.schedulePeriodicallyDownloadMatchKeys(this.applicationContext, matchKeysInterval)

                performMatchKeys()
            }

            else -> CentralLog.i(TAG, "Invalid / ignored command: $cmd. Nothing to do")
        }
    }

    private fun actionStop() {
        stopForeground(true)
        stopSelf()
        CentralLog.w(TAG, "Service Stopping")
    }

    private fun actionPurge() {
        val context = this
        launch {
            val before = System.currentTimeMillis() - purgeTTL
            CentralLog.i(TAG, "Coroutine - Purging of data before epoch time $before")

            streetPassRecordStorage.purgeOldRecords(before)
            statusRecordStorage.purgeOldRecords(before)
            tempIdStorage.purgeOldRecords(before)
            positiveKeysStorage.purgeOldRecords(before)

            Preference.putLastPurgeTime(context, System.currentTimeMillis())
        }
    }

    private fun actionStart() {

        CentralLog.d(TAG, "Action Start")

        commandHandler.scheduleNextScan(0)
        commandHandler.scheduleNextAdvertise(0)
        commandHandler.scheduleNextDownload(1000)
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
        advertiser = advertiser ?: BLEAdvertiser(serviceUUID)
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


    private fun scheduleDownload() {

        CentralLog.d(TAG,"scheduleDownload")

        if (!infiniteScanning) {
            commandHandler.scheduleNextDownload((Firebase.remoteConfig.getLong("download_duration_in_minutes") + Firebase.remoteConfig.getLong("download_interval_in_minutes")) * ONE_MIN)
        }
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        t.printStackTrace()
    }

    private fun performDownload() {

        CentralLog.d(TAG, "performDownload")


        //val context = this
        val appCtx = this.applicationContext

        launch(Dispatchers.IO + coroutineExceptionHandler) {

            var isMatchKeysRequiredToSchedule = false


            val c = Calendar.getInstance()
            c.timeInMillis = Preference.getInstallDateTS(appCtx)

            //val formattedInstallDate = "2020-04-22T19:39:03.744Z"
            val formattedInstallDate = Utils.formatCalendarToISO8601String(c)

            CentralLog.d(TAG, "PositiveKey install date = $formattedInstallDate")

            //TODO: comentat pentru ca dadea eroare retrofit

            val id = positiveKeysStorage.getLastId()
            val inst = BackendMethods.getInstance()

            val keys = when (id) {
                0 -> inst.getPositiveKeys(formattedInstallDate)
                else -> inst.getPositiveKeys(formattedInstallDate, id)
            }

            CentralLog.d(TAG, "PositiveKey downloaded keys size = ${keys.size}")

            var ownUploadApproved = false
            for (key in keys) {

                val keyDate = Calendar.getInstance()

                positiveKeysStorage.saveRecord(PositiveKeyRecord(key.id, key.tempId, keyDate))

                isMatchKeysRequiredToSchedule = true

                if (Preference.isUploadSent(appCtx))    //check only if upload was sent
                    if (tempIdStorage.checkIfPresent(key.tempId).size > 0)
                        ownUploadApproved = true;
            }

            if (isMatchKeysRequiredToSchedule) {

                CentralLog.d(TAG, "PositiveKey save done")
                CentralLog.d(TAG, "PositiveKey all records")

                for (pkr in positiveKeysStorage.getAllRecords()) {

                    CentralLog.d(TAG, "PositiveKey  key = ${pkr.key} ")
                }


                Utils.schedulePeriodicallyDownloadMatchKeys(appCtx, 200)
            }

            if (ownUploadApproved) {
                val n = NotificationTemplates.getOwnUploadApprovedNotification(
                    appCtx,
                    OWN_UPLOAD_CHANNEL_ID
                )

                with(NotificationManagerCompat.from(appCtx)) {
                    notify(OWN_UPLOAD_NOTIFICATION_ID, n)
                }

                Preference.putIsUploadComplete(appCtx, true)  //lock further uploading
            }
        }
    }

    private fun performMatchKeys() {

        CentralLog.d(TAG, "performMatchKeys")

        //val context = this
        val appCtx = this.applicationContext


        launch {

            //Uncomment next two lines only to fake data for test cases

//            BuildFakeContacts().run(appCtx)
//            infectionAlertRecordStorage.nukeDb()


//            cycleNoToNukeDb--
//
//            if (cycleNoToNukeDb == 0)
//                infectionAlertRecordStorage.nukeDb()



            val contacts: List<StreetPassRecord> = positiveKeysStorage.getMatchedKeysRecords(
                Firebase.remoteConfig.getLong("rssi_min_value").toInt())
            val alerts: List<InfectionAlertRecord> = infectionAlertRecordStorage.getAllRecords()
            val alg = ExposureAlgorithm(contacts)


            var hasNewAlerts = false

            for (d in alg.getExposureDays()) {

//                d("111 ----------------------------")
//                d(Utils.formatCalendarToISO8601String(d.date))
//                d("exposureInMinutes = ${d.exposureInMinutes}")
//                d("")

                val ed1 = d.date
                var isDayFound = false


                for (a in alerts) {

//                    d("222 ----")
//                    d(Utils.formatCalendarToISO8601String(a.exposureDate))
//                    d("exposureInMinutes = ${a.exposureInMinutes}")
//                    d("")

                    val ed2 = a.exposureDate

                    if (ed1.get(Calendar.YEAR) == ed2.get(Calendar.YEAR) &&
                        ed1.get(Calendar.MONTH) == ed2.get(Calendar.MONTH) &&
                        ed1.get(Calendar.DAY_OF_MONTH) == ed2.get(Calendar.DAY_OF_MONTH)) {

                        if (d.exposureInMinutes != a.exposureInMinutes) {

//                            d("updateExposureTime ${a.id} with exposureInMinutes = ${d.exposureInMinutes}")

                            infectionAlertRecordStorage.updateExposureTime(
                                a.id,
                                d.exposureInMinutes
                            )
                        }

                        isDayFound = true
                    }
                }


                if (!isDayFound) {

//                    d("saveRecord")

                    infectionAlertRecordStorage.saveRecord(
                        InfectionAlertRecord(
                        exposureDate = ed1,
                        exposureInMinutes = d.exposureInMinutes
                    )
                    )

                    hasNewAlerts = true
                }
            }



            if (hasNewAlerts) {

                CentralLog.d(TAG, "create exposure new alert notification")

                val n = NotificationTemplates.getExposureNewAlertsNotification(appCtx, NEW_ALERTS_CHANNEL_ID)
                //startForeground(NEW_ALTERS_NOTIFICATION_ID, n)
                startMainActivity()

                with(NotificationManagerCompat.from(appCtx)) {
                    notify(NEW_ALERTS_NOTIFICATION_ID, n)
                }
            }
        }
    }

    private fun startMainActivity() {
        //passing the notification here so in the future we can use information from it into the alert dialog
        val dialogIntent = Intent(this, MainActivity::class.java)
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(dialogIntent)
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

            if (!commandHandler.hasDownloadScheduled()) {
                CentralLog.w(TAG, "Missing Download Schedule - rectifying")
                commandHandler.scheduleNextDownload(100)
            } else {
                CentralLog.w(TAG, "Download Schedule present")
            }

        } else {
            CentralLog.w(TAG, "Should be operating under infinite scan mode")
        }

        if (!infiniteAdvertising) {
            if (!commandHandler.hasAdvertiseScheduled()) {
                CentralLog.w(TAG, "Missing Advertise Schedule - rectifying")
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

            tempIdStorage.saveRecord(
                TempId(
                    v = cti.tempID
                )
            )

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
                            Utils.startBluetoothMonitoringService(this@BluetoothMonitoringService.applicationContext)
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

                var selfC = ""
                if (connRecord.central.address?.indexOf("SELF") != -1)
                    selfC = " SELF"

                var selfP = ""
                if (connRecord.peripheral.address?.indexOf("SELF") != -1)
                    selfP = " SELF"

                if (connRecord.msg.isNotEmpty()) {
                    val record = StreetPassRecord(
                        v = connRecord.version,
                        msg = connRecord.msg,
                        org = connRecord.org,
                        modelP = connRecord.peripheral.modelP + selfP,
                        modelC = connRecord.central.modelC + selfC,
                        rssi = connRecord.rssi,
                        txPower = connRecord.txPower
                    )

                    launch {

                        streetPassRecordStorage.saveRecord(record)

                        CentralLog.d(TAG,
                            "StreetPassRecord save done " +
                                    "msg = ${record.msg} " +
                                    "ts = ${Utils.getDate(record.timestamp)}"
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
        ACTION_PURGE(6, "PURGE"),
        ACTION_DOWNLOAD(7, "DOWNLOAD"),
        ACTION_MATCH_KEYS(8, "MATCH_KEYS");

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
        private val CHANNEL_SERVICE = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME

        private const val NEW_ALERTS_NOTIFICATION_ID = 100002
        private const val NEW_ALERTS_CHANNEL_ID = "Exposure New Alerts ID"
        private const val NEW_ALERTS_CHANNEL_NAME = "Exposure New Alerts Name"

        private const val OWN_UPLOAD_NOTIFICATION_ID = 100003
        private const val OWN_UPLOAD_CHANNEL_ID = "Own Upload Approved ID"
        private const val OWN_UPLOAD_CHANNEL_NAME = "Own Upload Approved Name"

        val COMMAND_KEY = "${BuildConfig.APPLICATION_ID}_CMD"

        val PENDING_ACTIVITY = 5
        val PENDING_START = 6
        val PENDING_SCAN_REQ_CODE = 7
        val PENDING_ADVERTISE_REQ_CODE = 8
        val PENDING_HEALTH_CHECK_CODE = 9
        val PENDING_WIZARD_REQ_CODE = 10
        val PENDING_BM_UPDATE = 11
        val PENDING_PURGE_CODE = 12
        val PENDING_MATCH_KEYS_CODE = 13

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

        private const val ONE_MIN: Long = 60 * 1000             // In milliseconds
        val matchKeysInterval: Long = 3 * ONE_MIN
    }
}
