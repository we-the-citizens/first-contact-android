package ro.wethecitizens.firstcontact.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog

class MyStartServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        try {

            if (Intent.ACTION_BOOT_COMPLETED != intent!!.action) return

            // Start your service here.

            context?.let {
                CentralLog.i(
                    "MyStartServiceReceiver",
                    "Starting PeriodicallyDownloadJobService from MyStartServiceReceiver")

                Utils.schedulePeriodicallyDownloadJob(context)
            }
        } catch (e: Exception) {
            CentralLog.e("MyStartServiceReceiver", "Unable to handle upgrade: ${e.localizedMessage}")
        }
    }
}
