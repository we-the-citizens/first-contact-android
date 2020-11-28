// Copyright (c) 2020 BlueTrace.io

package ro.wethecitizens.firstcontact.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import ro.wethecitizens.firstcontact.utils.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.notifications.NotificationTemplates
import ro.wethecitizens.firstcontact.preference.Preference
import ro.wethecitizens.firstcontact.services.BluetoothMonitoringService

class DailyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        try {
            if (context == null || !Preference.isUploadSent(context))
                return;     //error, sanity

            if (Preference.isUploadComplete(context))
                return;     //we've received the confirmation that the document was approved, no need to do anything
            else
            {
                Preference.putIsUploadSent(context,false)   //clear the sent flag, so we can try again
                showDocumentRejectedNotification(context)         //show notification
            }

        } catch (e: Exception) {
            CentralLog.e("DailyAlarmReceiver", "Error at checking if document was rejected")
        }
    }

    fun showDocumentRejectedNotification(context: Context) {

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                REJECTED_CHANNEL_ID,
                REJECTED_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            ch.enableLights(false)
            ch.enableVibration(true)
            ch.setShowBadge(false)

            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager!!.createNotificationChannel(ch)
        }

        val n = NotificationTemplates.getOwnUploadRejectedNotification(context,REJECTED_CHANNEL_ID)

        with(NotificationManagerCompat.from(context)) {
            notify(REJECTED_NOTIFICATION_ID, n)
        }
    }

    private val REJECTED_NOTIFICATION_ID = 100009
    private val REJECTED_CHANNEL_ID = "Document Rejected ID"
    private val REJECTED_CHANNEL_NAME = "Document Rejected Name"
}
