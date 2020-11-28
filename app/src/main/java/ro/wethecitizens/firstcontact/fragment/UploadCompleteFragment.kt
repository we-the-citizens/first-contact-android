// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.fragment_upload_uploadcomplete.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.preference.Preference
import ro.wethecitizens.firstcontact.receivers.DailyAlarmReceiver


class UploadCompleteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_uploadcomplete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Preference.putIsUploadSent(view.context, true)
        registerAlarm(view.context)

        tv_message.movementMethod = ScrollingMovementMethod()

        uploadCompleteFragmentActionButton.setOnClickListener {
            (parentFragment as UploadPageFragment).goBackToHome()
        }
    }

    private fun registerAlarm(context: Context) {
        val MIN = 60 * 1000
        var minutesInterval = Firebase.remoteConfig.getLong("document_review_time_in_minutes").toInt()
        val intent = Intent(context, DailyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REJECT_CHECK_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutesInterval * MIN] = pendingIntent
    }

    private val REJECT_CHECK_REQUEST_CODE = 111221
}
