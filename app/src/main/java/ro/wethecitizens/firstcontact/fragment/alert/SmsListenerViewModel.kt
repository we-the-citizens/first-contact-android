package ro.wethecitizens.firstcontact.fragment.alert

import androidx.lifecycle.*
import com.google.android.gms.auth.api.phone.SmsRetrieverClient

/**
 * View Model shared via activity to keep listener alive.
 */
class SmsListenerViewModel : ViewModel() {

    private val mSmsText = MediatorLiveData<String>()
    val smsText: LiveData<String> = mSmsText

    private val mState = MutableLiveData<State>()
    val observableState: LiveData<State> = mState

    private var isAlreadyListening = false


    fun listenForSms(client: SmsRetrieverClient) {
        client.startSmsRetriever().apply {
            addOnSuccessListener {
                mState.postValue(State.ListeningForSms)

                startListening()
            }

            addOnFailureListener {
                mState.postValue(State.ListeningFailed)
            }
        }
    }

    private val dummyObserver = Observer<String> { }

    fun startListening() {

        if (isAlreadyListening)
            return

        isAlreadyListening = true

        with(mSmsText) {
            observeForever(dummyObserver)
            addSource(PinSmsBroadcastReceiver.observableSmsContent) { sms ->
                value = sms
                removeSource(PinSmsBroadcastReceiver.observableSmsContent)
                removeObserver(dummyObserver)

                // reset listener state for future uses
                mState.value = null
            }
        }
    }

    sealed class State {
        object ListeningForSms : State()
        object ListeningFailed : State()
    }
}