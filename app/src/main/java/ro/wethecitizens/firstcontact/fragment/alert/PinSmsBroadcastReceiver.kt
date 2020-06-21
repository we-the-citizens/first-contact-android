// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment.alert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import ro.wethecitizens.firstcontact.utils.SingleLiveEvent

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
        private val mSmsContent = SingleLiveEvent<String>()
        val observableSmsContent: LiveData<String> = mSmsContent
    }
}