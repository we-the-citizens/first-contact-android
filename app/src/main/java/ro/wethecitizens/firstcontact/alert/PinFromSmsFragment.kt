package ro.wethecitizens.firstcontact.alert

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pin_from_sms.view.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.alert.PinFromSmsViewModel.State.*
import ro.wethecitizens.firstcontact.alert.SmsListenerViewModel.State.ListeningFailed
import ro.wethecitizens.firstcontact.alert.SmsListenerViewModel.State.ListeningForSms

class PinFromSmsFragment : Fragment(R.layout.fragment_pin_from_sms) {

    private lateinit var mSharedViewModel: SmsListenerViewModel
    private val smsObserver = Observer<SmsListenerViewModel.State> { state ->
        when (state) {
            ListeningForSms -> {
                view?.sms_pin_input?.setHint(R.string.listening_for_sms)
//                view?.sms_confirmation_button?.isEnabled = false
            }
            else -> {
                view?.sms_pin_input?.setHint(R.string.pin_from_sms)
//                view?.sms_confirmation_button?.isEnabled = true
            }
        }
    }

    private lateinit var mViewModel: PinFromSmsViewModel
    private val stateObserver = Observer<PinFromSmsViewModel.State> { state ->
        when (state) {
            InvalidSms -> view?.sms_pin_input?.setHint(R.string.pin_from_sms)
            is ValidSms -> {
                view?.loading_layout?.visibility = View.VISIBLE
                view?.sms_pin_input?.setText(state.pin)
            }

            is UploadFailed -> {
                view?.loading_layout?.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    getString(
                        R.string.upload_failed,
                        state.errorType.code.toString(),
                        state.errorType::class.java.simpleName
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }

            IdsUploaded -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.upload_success),
                    Toast.LENGTH_LONG
                ).show()

                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appCtx: Context = view.context


        mViewModel = ViewModelProvider(this).get(PinFromSmsViewModel::class.java)

        mViewModel.observableState.observe(viewLifecycleOwner, stateObserver)

        mSharedViewModel =
            ViewModelProvider(requireActivity()).get(SmsListenerViewModel::class.java)

        mSharedViewModel.observableState.observe(viewLifecycleOwner, smsObserver)

        mSharedViewModel.smsText.observe(viewLifecycleOwner, Observer { sms ->
            mViewModel.handleSms(sms, appCtx)
        })


        view.sms_confirmation_button.setOnClickListener {
            view.sms_pin_input.text?.takeIf { it.isNotEmpty() }?.let {
                view.loading_layout?.visibility = View.VISIBLE
                mViewModel.uploadContacts(it.toString(), appCtx)
            } ?: run {
                Toast.makeText(
                    it.context,
                    getString(R.string.add_pin_before_upload),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}