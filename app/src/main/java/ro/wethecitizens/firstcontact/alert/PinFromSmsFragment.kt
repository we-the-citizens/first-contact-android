package ro.wethecitizens.firstcontact.alert

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import kotlinx.android.synthetic.main.fragment_pin_from_sms.view.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.alert.PinFromSmsViewModel.State.*

class PinFromSmsFragment : Fragment(R.layout.fragment_pin_from_sms) {

    private lateinit var mViewModel: PinFromSmsViewModel
    private val stateObserver = Observer<PinFromSmsViewModel.State> { state ->
        when (state) {
            ListeningForSms -> {
                waitForBroadcast()
                view?.sms_pin_input?.setHint(R.string.listening_for_sms)
            }
            InvalidSms, ListeningFailed -> view?.sms_pin_input?.setHint(R.string.pin_from_sms)
            is ValidSms -> TODO("trigger server request")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(PinFromSmsViewModel::class.java)
        mViewModel.observableState.observe(viewLifecycleOwner, stateObserver)

        // start sms listener
        val client: SmsRetrieverClient = SmsRetriever.getClient(view.context)
        mViewModel.listenForSms(client)

        arguments?.getString(getString(R.string.qr_code))?.let { code ->
            mViewModel.setQrCode(code)
        } ?: run {
            Toast.makeText(requireContext(), getString(R.string.no_qr_code), Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        }
    }

    private var listening = false

    private fun waitForBroadcast() {
        if (listening) return
        PinSmsBroadcastReceiver.observableSmsContent.observe(viewLifecycleOwner, Observer {
            mViewModel.handleSms(it)
        })
        listening = true
    }
}