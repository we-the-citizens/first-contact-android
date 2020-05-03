package ro.wethecitizens.firstcontact.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ro.wethecitizens.firstcontact.MainActivity
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.onboarding.OnboardingActivity
import ro.wethecitizens.firstcontact.services.BluetoothMonitoringService.Companion.PENDING_ACTIVITY
import ro.wethecitizens.firstcontact.services.BluetoothMonitoringService.Companion.PENDING_WIZARD_REQ_CODE

class NotificationTemplates {

    companion object {

        fun getStartupNotification(context: Context, channel: String): Notification {

            val builder = NotificationCompat.Builder(context, channel)
                .setContentText("Tracer is setting up its antennas")
                .setContentTitle("Setting things up")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_notification_setting)
                .setWhen(System.currentTimeMillis())
                .setSound(null)
                .setVibrate(null)
                .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }

        fun getRunningNotification(context: Context, channel: String): Notification {

            var intent = Intent(context, MainActivity::class.java)

            val activityPendingIntent = PendingIntent.getActivity(
                context, PENDING_ACTIVITY,
                intent, 0
            )

            val builder = NotificationCompat.Builder(context, channel)
                .setContentTitle(context.getText(R.string.service_ok_title))
                .setContentText(context.getText(R.string.service_ok_body))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_notification_service)
                .setContentIntent(activityPendingIntent)
                .setTicker(context.getText(R.string.service_ok_body))
                .setStyle(NotificationCompat.BigTextStyle().bigText(context.getText(R.string.service_ok_body)))
                .setWhen(System.currentTimeMillis())
                .setSound(null)
                .setVibrate(null)
                .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }

        fun lackingThingsNotification(context: Context, channel: String): Notification {
            var intent = Intent(context, OnboardingActivity::class.java)
            intent.putExtra("page", 3)

            val activityPendingIntent = PendingIntent.getActivity(
                context, PENDING_WIZARD_REQ_CODE,
                intent, 0
            )

            val builder = NotificationCompat.Builder(context, channel)
                .setContentTitle(context.getText(R.string.service_not_ok_title))
                .setContentText(context.getText(R.string.service_not_ok_body))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_notification_warning)
                .setTicker(context.getText(R.string.service_not_ok_body))
                .addAction(
                    R.drawable.ic_notification_setting,
                    context.getText(R.string.service_not_ok_action),
                    activityPendingIntent
                )
                .setContentIntent(activityPendingIntent)
                .setWhen(System.currentTimeMillis())
                .setSound(null)
                .setVibrate(null)
                .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }

        fun getExposureNewAlertsNotification(context: Context, channel: String): Notification {

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, PENDING_ACTIVITY, intent, 0)

//            val intent = Intent(context, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            }
//            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)


            val title = context.getText(R.string.exposure_new_alerts_title)
            val body = context.getText(R.string.exposure_new_alerts_body)

            val b = NotificationCompat.Builder(context, channel)
                .setContentTitle(title)
                .setContentText(body)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification_warning)
                .setContentIntent(pendingIntent)
                .setTicker(body)
                //.setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setWhen(System.currentTimeMillis())
                .setVibrate(longArrayOf(0, 1000, 1000, 1000, 1000))
                .setLights(Color.RED, 3000, 3000)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)

            return b.build()
        }
    }
}
