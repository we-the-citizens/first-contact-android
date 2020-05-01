package ro.wethecitizens.firstcontact.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import ro.wethecitizens.firstcontact.utils.SingleLiveEvent

/**
 * View Model shared via activity to keep listener alive.
 */
class SmsListenerViewModel : ViewModel() {

    private val mSmsText = MediatorLiveData<String>()
    val smsText: LiveData<String> = mSmsText

    private val mState = SingleLiveEvent<State>()
    val observableState: LiveData<State> = mState

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
        with(mSmsText) {
            observeForever(dummyObserver)
            addSource(PinSmsBroadcastReceiver.observableSmsContent) { sms ->
                value = sms
                removeSource(PinSmsBroadcastReceiver.observableSmsContent)
                removeObserver(dummyObserver)
            }
        }
    }

    sealed class State {
        object ListeningForSms : State()
        object ListeningFailed : State()
    }
}