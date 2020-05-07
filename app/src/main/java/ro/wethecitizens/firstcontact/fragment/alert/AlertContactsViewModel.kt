package ro.wethecitizens.firstcontact.fragment.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.wethecitizens.firstcontact.Preference
import ro.wethecitizens.firstcontact.fragment.alert.server.AuthorizationRequest
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.server.HttpCode
import ro.wethecitizens.firstcontact.utils.AppSignatureHelper
import ro.wethecitizens.firstcontact.utils.SingleLiveEvent
import java.lang.Exception
import java.util.*

class AlertContactsViewModel : ViewModel() {

    private val mState: SingleLiveEvent<State> = SingleLiveEvent()
    val observableState: LiveData<State> = mState

    fun setQRCode(qrCode: String) {

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

            try {
                val response = BackendMethods.getInstance().checkUploadAuthorization(
                    AppSignatureHelper.getAppHash(),
                    requestBody
                )

                when (response.isSuccessful) {
                    true -> mState.postValue(State.Success)

                    else -> {
                        val errorCode = response.code()

                        val errorType = HttpCode.getType(errorCode)

                        mState.postValue(State.Failed(errorType))
                    }
                }
            } catch (e: Exception) {
                mState.postValue(State.Failed(HttpCode.UNKNOWN_ERROR(-1)))
                e.printStackTrace()
            }
        }
    }

    sealed class State {
        class Loading(val qrCode: String) : State()
        object Success : State()
        class Failed(val errorType: HttpCode) : State()
    }
}
