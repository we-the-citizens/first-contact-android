package ro.wethecitizens.firstcontact.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.wethecitizens.firstcontact.Preference
import ro.wethecitizens.firstcontact.alert.server.PositiveIdsRequest
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.server.HttpCode
import ro.wethecitizens.firstcontact.utils.SingleLiveEvent
import java.util.regex.Pattern

class PinFromSmsViewModel : ViewModel() {

    private val mState: SingleLiveEvent<State> = SingleLiveEvent()
    val observableState: LiveData<State> = mState

    fun handleSms(smsContent: String?) {
        when (smsContent) {
            null -> mState.value = State.InvalidSms
            else -> {
                retrievePinFromSms(smsContent)?.let { pin ->
                    mState.value = State.ValidSms(pin)

                    uploadContacts(pin)
                } ?: run {
                    mState.value = State.InvalidSms
                }
            }
        }
    }

    fun uploadContacts(pinCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val pacientId = Preference.getPatientIdQr()!!

            val request = PositiveIdsRequest(
                pacientId,
                pinCode,

                // FIXME: Load from database!!!

                listOf(
                    PositiveIdsRequest.PositiveId(
                        tempId = "TEST_10_2_10",
                        date = "2020-04-21T00:11:22.333Z"
                    )
                )
            )

            try {
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
            } catch (e: Exception) {
                mState.postValue(State.UploadFailed(HttpCode.UNKNOWN_ERROR(-1)))
                e.printStackTrace()
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

    sealed class State {
        object InvalidSms : State()
        class ValidSms(val pin: String) : State()

        object IdsUploaded : State()
        class UploadFailed(val errorType: HttpCode) : State()
    }
}