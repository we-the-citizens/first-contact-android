package ro.wethecitizens.firstcontact.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import ro.wethecitizens.firstcontact.utils.SingleLiveEvent
import java.util.regex.Pattern

class PinFromSmsViewModel : ViewModel() {

    private lateinit var qrCode: String

    private val mState: SingleLiveEvent<State> = SingleLiveEvent()
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
            else -> {
                retrievePinFromSms(smsContent)?.let { pin ->
                    mState.value = State.ValidSms

                    uploadContacts(pin)
                } ?: run {
                    mState.value = State.InvalidSms
                }
            }
        }
    }

    private fun uploadContacts(pinCode: String) {
        // TODO()
    }

    /**
     * PIN Code should be the first number in the sms string.
     */
    fun retrievePinFromSms(smsContent: String): String? {
        return Pattern.compile("\\d+")
            .matcher(smsContent)
            .takeIf { it.find() }
            ?.group(0)
    }

    fun setQrCode(qrCode: String) {
        this.qrCode = qrCode
    }

    sealed class State {
        object ListeningForSms : State()
        object ListeningFailed : State()
        object InvalidSms : State()
        object ValidSms : State()
    }
}