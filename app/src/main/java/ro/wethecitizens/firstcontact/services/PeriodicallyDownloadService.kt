package ro.wethecitizens.firstcontact.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ro.wethecitizens.firstcontact.BuildConfig
import ro.wethecitizens.firstcontact.bluetooth.gatt.ACTION_RECEIVED_STATUS
import ro.wethecitizens.firstcontact.bluetooth.gatt.STATUS
import ro.wethecitizens.firstcontact.idmanager.TemporaryID
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.notifications.NotificationTemplates
import ro.wethecitizens.firstcontact.status.Status
import ro.wethecitizens.firstcontact.status.persistence.StatusRecord
import ro.wethecitizens.firstcontact.status.persistence.StatusRecordStorage
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class PeriodicallyDownloadService : Service(), CoroutineScope {


    /* Private members */

    private var mNotificationManager: NotificationManager? = null
    private val statusReceiver = StatusReceiver()
    private var job: Job = Job()
    private var notificationShown: NOTIFICATION_STATE? = null

    private lateinit var statusRecordStorage: StatusRecordStorage
    private lateinit var commandHandler: PDCommandHandler
    private lateinit var localBroadcastManager: LocalBroadcastManager




    /* Override */

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        setup()
    }

    override fun onDestroy() {

        super.onDestroy()

        CentralLog.i(TAG, "BluetoothMonitoringService destroyed - tearing down")
        stopService()
        CentralLog.i(TAG, "BluetoothMonitoringService destroyed")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        CentralLog.i(TAG, "Service onStartCommand")

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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }



    /* Private fun */

    fun setup() {

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        CentralLog.setPowerManager(pm)

        commandHandler = PDCommandHandler(WeakReference(this))

        CentralLog.d(TAG, "Creating service - BluetoothMonitoringService")

        //worker = StreetPassWorker(this.applicationContext)

        unregisterReceivers()
        registerReceivers()

        statusRecordStorage = StatusRecordStorage(this.applicationContext)

        setupNotifications()
    }

    fun teardown() {

        commandHandler.removeCallbacksAndMessages(null)
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

    fun runService(cmd: Command?) {

        var doWork = true
        CentralLog.i(TAG, "Command is:${cmd?.string}")


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

        CentralLog.d(TAG, "actionStart")

//        TempIDManager.getTemporaryIDs(this, functions)
//            .addOnCompleteListener {
//                CentralLog.d(TAG, "Get TemporaryIDs completed")
//                //this will run whether it starts or fails.
//                var fetch = TempIDManager.retrieveTemporaryID(this.applicationContext)
//                fetch?.let {
//                    broadcastMessage = it
//                    setupCycles()
//                }
//            }
    }

    fun actionUpdateBm() {

        CentralLog.d(TAG, "actionUpdateBm")

//        if (TempIDManager.needToUpdate(this.applicationContext) || broadcastMessage == null) {
//            CentralLog.i(TAG, "[TempID] Need to update TemporaryID in actionUpdateBM")
//            //need to pull new BM
//            TempIDManager.getTemporaryIDs(this, functions)
//                .addOnCompleteListener {
//                    //this will run whether it starts or fails.
//                    var fetch = TempIDManager.retrieveTemporaryID(this.applicationContext)
//                    fetch?.let {
//                        CentralLog.i(TAG, "[TempID] Updated Temp ID")
//                        broadcastMessage = it
//                    }
//
//                    if (fetch == null) {
//                        CentralLog.e(TAG, "[TempID] Failed to fetch new Temp ID")
//                    }
//                }
//        } else {
//            CentralLog.i(TAG, "[TempID] Don't need to update Temp ID in actionUpdateBM")
//        }

    }

    fun calcPhaseShift(min: Long, max: Long): Long {

        return (min + (Math.random() * (max - min))).toLong()
    }

    private fun actionScan() {

        CentralLog.i(TAG, "actionScan")

//        if (TempIDManager.needToUpdate(this.applicationContext) || broadcastMessage == null) {
//            CentralLog.i(TAG, "[TempID] Need to update TemporaryID in actionScan")
//            //need to pull new BM
//            TempIDManager.getTemporaryIDs(this.applicationContext, functions)
//                .addOnCompleteListener {
//                    //this will run whether it starts or fails.
//                    var fetch = TempIDManager.retrieveTemporaryID(this.applicationContext)
//                    fetch?.let {
//                        broadcastMessage = it
//                        performScan()
//                    }
//                }
//        } else {
//            CentralLog.i(TAG, "[TempID] Don't need to update Temp ID in actionScan")
//            performScan()
//        }
    }

    private fun actionAdvertise() {

        CentralLog.d(TAG, "actionAdvertise")
    }

    private fun setupService() {

        CentralLog.d(TAG, "setupService")

        setupScanner()
        setupAdvertiser()
    }

    private fun setupScanner() {

        CentralLog.d(TAG, "setupScanner")
    }

    private fun setupAdvertiser() {

        CentralLog.d(TAG, "setupAdvertiser")
    }

    private fun setupCycles() {

        CentralLog.d(TAG, "setupCycles")

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

        CentralLog.d(TAG, "scheduleScan")

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

        CentralLog.d(TAG, "scheduleScan")

        if (!infiniteAdvertising) {
            commandHandler.scheduleNextAdvertise(advertisingDuration + advertisingGap)
        }
    }

    private fun startScan() {

        CentralLog.d(TAG, "startScan")
    }

    private fun performHealthCheck() {

        CentralLog.i(TAG, "Performing self diagnosis")

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

    }

    private fun performPurge() {

        val context = this
        launch {
            val before = System.currentTimeMillis() - purgeTTL
            CentralLog.i(TAG, "Coroutine - Purging of data before epoch time $before")

            statusRecordStorage.purgeOldRecords(before)
            ro.wethecitizens.firstcontact.Preference.putLastPurgeTime(context, System.currentTimeMillis())
        }
    }

    private fun stopService() {

        teardown()
        unregisterReceivers()

        job.cancel()
    }


    private fun registerReceivers() {

        val statusReceivedFilter = IntentFilter(ACTION_RECEIVED_STATUS)
        localBroadcastManager.registerReceiver(statusReceiver, statusReceivedFilter)

        CentralLog.i(TAG, "Receivers registered")
    }

    private fun unregisterReceivers() {

        try {
            localBroadcastManager.unregisterReceiver(statusReceiver)
        } catch (e: Throwable) {
            CentralLog.w(TAG, "statusReceiver is not registered?")
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

        private val TAG = "PDService"

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
        val bmValidityCheck = false
    }
}
