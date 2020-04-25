package ro.wethecitizens.firstcontact.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ro.wethecitizens.firstcontact.BuildConfig
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.notifications.NotificationTemplates
import ro.wethecitizens.firstcontact.positivekey.persistence.PositiveKeyRecordStorage
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class PeriodicallyDownloadService : Service(), CoroutineScope {


    /* Private members */

    private var mNotificationManager: NotificationManager? = null
    private var job: Job = Job()
    private var notificationShown: NotificationState? = null

    private lateinit var positiveKeysStorage: PositiveKeyRecordStorage
    private lateinit var commandHandler: PeriodicallyDownloadCommandHandler
    private lateinit var localBroadcastManager: LocalBroadcastManager




    /* Override */

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate() {

        d("onCreate")

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        setup()
    }

    override fun onDestroy() {

        super.onDestroy()

        d("onDestroy")

        destroyService()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        d("onStartCommand")

        intent?.let {
            val cmd = intent.getIntExtra(COMMAND_KEY, Command.INVALID.index)
            runService(Command.findByValue(cmd))

            return START_STICKY
        }

        if (intent == null) {
            CentralLog.e(TAG, "WTF? Nothing in intent @ onStartCommand")
            commandHandler.startPeriodicallyDownloadService()
        }

        // Tells the system to not try to recreate the service after it has been killed.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }



    /* Private fun */

    fun d(s:String) {

        CentralLog.d(TAG, s)
    }

    private fun setup() {

        d("setup")


        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        CentralLog.setPowerManager(pm)

        commandHandler = PeriodicallyDownloadCommandHandler(WeakReference(this))

        positiveKeysStorage = PositiveKeyRecordStorage(this.applicationContext)

        setupNotifications()
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

    private fun notifyRunning(override: Boolean = false) {

        if (notificationShown != NotificationState.RUNNING || override) {
            val notif =
                NotificationTemplates.getRunningNotification(this.applicationContext, CHANNEL_ID)
            startForeground(NOTIFICATION_ID, notif)
            notificationShown = NotificationState.RUNNING
        }
    }

    fun runService(cmd: Command?) {

        d("runService")
        d("Command is:${cmd?.string}")


        var doWork = true

        //show running foreground notification if its not showing that
        notifyRunning()

        when (cmd) {

            Command.ACTION_START -> {

                Utils.schedulePeriodicallyDownloadNextHealthCheck(this.applicationContext, healthCheckInterval)
                Utils.schedulePeriodicallyDownloadRepeatingPurge(this.applicationContext, purgeInterval)
                Utils.schedulePeriodicallyDownloadMatchKeys(this.applicationContext, matchKeysInterval)

                performStart()
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

            Command.ACTION_SELF_CHECK -> {

                Utils.schedulePeriodicallyDownloadNextHealthCheck(this.applicationContext, healthCheckInterval)

                if (doWork) {

                    performHealthCheck()
                    Utils.schedulePeriodicallyDownloadRepeatingPurge(this.applicationContext, purgeInterval)
                }
            }

            Command.ACTION_PURGE -> {

                performPurge()
            }

            Command.ACTION_STOP -> {

                stopForeground(true)
                stopSelf()
            }

            else -> d("Invalid / ignored command: $cmd. Nothing to do")
        }
    }


    private fun performStart() {

        d("performStart")

//        TempIDManager.getTemporaryIDs(this, functions)
//            .addOnCompleteListener {
//                d("Get TemporaryIDs completed")
//                //this will run whether it starts or fails.
//                var fetch = TempIDManager.retrieveTemporaryID(this.applicationContext)
//                fetch?.let {
//                    broadcastMessage = it
//                    setupCycles()
//                }
//            }

        commandHandler.scheduleNextDownload(1000)
    }

    private fun scheduleDownload() {

        d("scheduleDownload")

        if (!infiniteScanning) {
            commandHandler.scheduleNextDownload(
                scanDuration + calcPhaseShift(
                    minScanInterval,
                    maxScanInterval
                )
            )
        }
    }

    private fun performDownload() {

        d("performDownload")

//        if (TempIDManager.needToUpdate(this.applicationContext) || broadcastMessage == null) {
//            d("[TempID] Need to update TemporaryID in actionScan")
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
//            d("[TempID] Don't need to update Temp ID in actionScan")
//            performScan()
//        }
    }

    private fun performMatchKeys() {

        d("performMatchKeys")

//        if (TempIDManager.needToUpdate(this.applicationContext) || broadcastMessage == null) {
//            d("[TempID] Need to update TemporaryID in actionUpdateBM")
//            //need to pull new BM
//            TempIDManager.getTemporaryIDs(this, functions)
//                .addOnCompleteListener {
//                    //this will run whether it starts or fails.
//                    var fetch = TempIDManager.retrieveTemporaryID(this.applicationContext)
//                    fetch?.let {
//                        d("[TempID] Updated Temp ID")
//                        broadcastMessage = it
//                    }
//
//                    if (fetch == null) {
//                        CentralLog.e(TAG, "[TempID] Failed to fetch new Temp ID")
//                    }
//                }
//        } else {
//            d("[TempID] Don't need to update Temp ID in actionUpdateBM")
//        }

    }

    private fun performHealthCheck() {

        d("performHealthCheck")


        notifyRunning(true)


        if (!infiniteScanning) {
            if (!commandHandler.hasScanScheduled()) {
                CentralLog.w(TAG, "Missing Scan Schedule - rectifying")
                commandHandler.scheduleNextDownload(100)
            } else {
                CentralLog.w(TAG, "Scan Schedule present")
            }
        } else {
            CentralLog.w(TAG, "Should be operating under infinite scan mode")
        }

    }

    private fun performPurge() {

        d("performPurge")

        val context = this

        launch {

            val before = System.currentTimeMillis() - purgeTTL
            d("Coroutine - Purging of data before epoch time $before")

            positiveKeysStorage.purgeOldRecords(before)

            ro.wethecitizens.firstcontact.Preference.putLastPurgeTime(context, System.currentTimeMillis())
        }
    }


    private fun destroyService() {

        d("destroyService")

        commandHandler.removeCallbacksAndMessages(null)

        job.cancel()
    }

    private fun calcPhaseShift(min: Long, max: Long): Long {

        return (min + (Math.random() * (max - min))).toLong()
    }


    enum class Command(val index: Int, val string: String) {
        INVALID(-1, "INVALID"),
        ACTION_START(0, "START"),
        ACTION_DOWNLOAD(1, "DOWNLOAD"),
        ACTION_STOP(2, "STOP"),
        ACTION_SELF_CHECK(4, "SELF_CHECK"),
        ACTION_MATCH_KEYS(5, "MATCH_KEYS"),
        ACTION_PURGE(6, "PURGE");

        companion object {
            private val types = values().associateBy { it.index }
            fun findByValue(value: Int) = types[value]
        }
    }

    enum class NotificationState {

        RUNNING
    }

    companion object {

        private const val TAG = "PDService"

        private const val NOTIFICATION_ID = BuildConfig.SERVICE_FOREGROUND_NOTIFICATION_ID
        private const val CHANNEL_ID = BuildConfig.SERVICE_FOREGROUND_CHANNEL_ID
        const val CHANNEL_SERVICE = BuildConfig.SERVICE_FOREGROUND_CHANNEL_NAME

        const val COMMAND_KEY = "${BuildConfig.APPLICATION_ID}_CMD"

        const val PENDING_HEALTH_CHECK_CODE = 9
        const val PENDING_MATCH_KEYS_CODE = 11
        const val PENDING_PURGE_CODE = 12

        const val scanDuration: Long = BuildConfig.SCAN_DURATION
        const val minScanInterval: Long = BuildConfig.MIN_SCAN_INTERVAL
        const val maxScanInterval: Long = BuildConfig.MAX_SCAN_INTERVAL

        const val matchKeysInterval: Long = BuildConfig.BM_CHECK_INTERVAL
        const val healthCheckInterval: Long = BuildConfig.HEALTH_CHECK_INTERVAL
        const val purgeInterval: Long = BuildConfig.PURGE_INTERVAL
        const val purgeTTL: Long = BuildConfig.PURGE_TTL
        const val infiniteScanning = false
    }
}
