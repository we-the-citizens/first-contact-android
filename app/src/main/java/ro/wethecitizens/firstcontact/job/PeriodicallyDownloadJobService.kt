package ro.wethecitizens.firstcontact.job

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog

class PeriodicallyDownloadJobService : JobService() {

    private val TAG = "PeriodicallyDownloadJobService"

    override fun onStartJob(params: JobParameters?): Boolean {

//        val service = Intent(applicationContext, PeriodicallyDownload::class)
//        applicationContext.startService(service)
//        Utils.schedulePeriodicallyDownloadJob(applicationContext)

        CentralLog.d(TAG, "onStartJob")

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {

        CentralLog.d(TAG, "onStopJob")

        return true
    }
}