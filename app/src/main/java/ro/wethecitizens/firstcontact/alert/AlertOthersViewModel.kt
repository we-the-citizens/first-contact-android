package ro.wethecitizens.firstcontact.alert

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.zxing.integration.android.IntentResult

class AlertOthersViewModel : ViewModel() {

    fun getScanInfo(result: IntentResult?) {
        if (result == null) {
            Log.d("Alert", "result null")
            return
        }

        val content = result.contents
        if (content == null) {
            Log.d("Alert", "scan did not return anything")
            return
        }

        Log.d("Alert", "Content of scan: $content")
    }
}