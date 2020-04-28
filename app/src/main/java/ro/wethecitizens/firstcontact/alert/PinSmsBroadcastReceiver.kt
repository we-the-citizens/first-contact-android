package ro.wethecitizens.firstcontact.alert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class PinSmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get SMS message contents
                    if (mSmsContent.hasActiveObservers()) {
                        mSmsContent.value = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    mSmsContent.value = null
                }
            }
        }
    }

    companion object {
        private val mSmsContent = MutableLiveData<String>()
        val observableSmsContent: LiveData<String> = mSmsContent
    }
}