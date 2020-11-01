// Copyright (c) 2020 BlueTrace.io

package ro.wethecitizens.firstcontact.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ro.wethecitizens.firstcontact.utils.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog

class StartOnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            CentralLog.d("StartOnBootReceiver", "boot completed received")

            try {
                //can i try a scheduled service start here?
                CentralLog.d("StartOnBootReceiver", "Attempting to start service")
               Utils.scheduleStartMonitoringService(context, 500)
            } catch (e: Throwable) {
                CentralLog.e("StartOnBootReceiver", e.localizedMessage)
                e.printStackTrace()
            }

        }
    }
}
