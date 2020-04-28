package ro.wethecitizens.firstcontact.alert

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import kotlinx.android.synthetic.main.fragment_pin_from_sms.view.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.alert.PinFromSmsViewModel.State.ListeningFailed
import ro.wethecitizens.firstcontact.alert.PinFromSmsViewModel.State.ListeningForSms

class PinFromSmsFragment : Fragment(R.layout.fragment_pin_from_sms) {

    private lateinit var mViewModel: PinFromSmsViewModel
    private val stateObserver = Observer<PinFromSmsViewModel.State> { state ->
        when (state) {
            ListeningForSms -> view?.sms_pin_input?.setHint(R.string.listening_for_sms)
            ListeningFailed -> view?.sms_pin_input?.setHint(R.string.pin_from_sms)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(PinFromSmsViewModel::class.java)
        mViewModel.observableState.observe(viewLifecycleOwner, stateObserver)

        // start sms listener
        val client: SmsRetrieverClient = SmsRetriever.getClient(view.context)
        mViewModel.listenForSms(client)

        view.sms_confirmation_button.setOnClickListener {

        }
    }
}