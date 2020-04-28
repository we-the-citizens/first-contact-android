package ro.wethecitizens.firstcontact.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.phone.SmsRetrieverClient

class PinFromSmsViewModel : ViewModel() {

    private val mState: MutableLiveData<State> = MutableLiveData()
    val observableState: LiveData<State> = mState

    fun listenForSms(client: SmsRetrieverClient) {

        client.startSmsRetriever().apply {
            addOnSuccessListener {
                mState.postValue(State.ListeningForSms)
            }

            addOnFailureListener {
                mState.postValue(State.ListeningFailed)
            }
        }
    }

    fun handleSms(smsContent: String?) {
        when (smsContent) {
            null -> mState.value = State.InvalidSms
            else -> mState.value = State.ValidSms(retrievePinFromSms(smsContent))
        }
    }

    fun retrievePinFromSms(smsContent: String): String {
        // FIXME: need sms format
        return smsContent
    }

    sealed class State {
        object ListeningForSms : State()
        object ListeningFailed : State()
        object InvalidSms : State()
        class ValidSms(val pinCode: String) : State()
    }
}