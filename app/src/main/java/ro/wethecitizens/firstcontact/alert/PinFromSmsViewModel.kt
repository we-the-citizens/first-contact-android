package ro.wethecitizens.firstcontact.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.wethecitizens.firstcontact.Preference
import ro.wethecitizens.firstcontact.alert.server.PositiveIdsRequest
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.server.HttpCode
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
        viewModelScope.launch(Dispatchers.IO) {
            val pacientId = Preference.getPatientIdQr()!!

            val request = PositiveIdsRequest(
                pacientId,
                pinCode,

                // FIXME: Load from database!!!

                listOf(
                    PositiveIdsRequest.PositiveId(
                        tempId = "TEST_10_2_10",
                        date = "2020-04-25T04:20:69.278Z"
                    )
                )
            )

            val response = BackendMethods.getInstance().uploadPositiveIds(request)

            when (response.code()) {
                HttpCode.OK.code ->
                    mState.postValue(State.IdsUploaded)
                else -> {
                    val errorCode = response.code()

                    val errorType = HttpCode.getType(errorCode)

                    mState.postValue(State.UploadFailed(errorType))
                }
            }
        }
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

        object IdsUploaded : State()
        class UploadFailed(val errorType: HttpCode) : State()
    }
}