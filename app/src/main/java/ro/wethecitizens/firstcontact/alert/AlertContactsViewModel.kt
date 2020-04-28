package ro.wethecitizens.firstcontact.alert

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ro.wethecitizens.firstcontact.Preference
import java.util.*

class AlertContactsViewModel : ViewModel() {

    private val mState: MutableLiveData<State> = MutableLiveData()
    val observableState: LiveData<State> = mState

    fun getScanInfo(result: IntentResult?) {
        if (result == null) {
            Log.d("Alert", "result null")
            return
        }

        val qrCode = result.contents
        if (qrCode == null) {
            Log.d("Alert", "scan did not return anything")
            return
        }

        viewModelScope.launch {

            val patientId = UUID.randomUUID().toString()
                .also { Preference.putPatientIdQr(it) }

            mState.value = State.Loading
            val response = delay(5000)
//            FIXME: use server method when implementation is ready

            mState.value = State.Success
        }
    }

    sealed class State {
        object Loading: State()
        object Success: State()
        object Failed: State()
    }
}