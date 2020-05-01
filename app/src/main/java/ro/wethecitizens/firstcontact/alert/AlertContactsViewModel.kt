package ro.wethecitizens.firstcontact.alert

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.wethecitizens.firstcontact.Preference
import ro.wethecitizens.firstcontact.alert.server.AuthorizationRequest
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.server.HttpCode
import ro.wethecitizens.firstcontact.utils.SingleLiveEvent
import java.util.*

class AlertContactsViewModel : ViewModel() {

    private val mState: SingleLiveEvent<State> = SingleLiveEvent()
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

        mState.value = State.Loading(qrCode)
    }

    fun checkAuthorization(qrCode: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val patientId = UUID.randomUUID().toString()
                // store the id locally to reuse later in sms screen
                .also { Preference.putPatientIdQr(it) }

            val requestBody = AuthorizationRequest(
                patientId,
                qrCode
            )

            val response = BackendMethods.getInstance().checkUploadAuthorization(requestBody)

            when (response.code()) {
                HttpCode.OK.code ->
                    mState.postValue(State.Success)
                else -> {
                    val errorCode = response.code()

                    val errorType = HttpCode.getType(errorCode)

                    mState.postValue(State.Failed(errorType))
                }
            }
        }
    }

    sealed class State {
        class Loading(val qrCode: String) : State()
        object Success : State()
        class Failed(val errorType: HttpCode) : State()
    }
}
