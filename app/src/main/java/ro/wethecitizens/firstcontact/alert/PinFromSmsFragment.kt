package ro.wethecitizens.firstcontact.alert

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

class PinFromSmsFragment : Fragment(R.layout.fragment_pin_from_sms) {

    private lateinit var mViewModel: PinFromSmsViewModel
    private val stateObserver = Observer<PinFromSmsViewModel.State> { state ->
        when (state) {
            ListeningForSms -> view?.sms_pin_input?.setHint(R.string.listening_for_sms)
            InvalidSms, ListeningFailed -> view?.sms_pin_input?.setHint(R.string.pin_from_sms)
            ValidSms -> view?.loading_layout?.visibility = View.VISIBLE

            is UploadFailed -> Toast.makeText(
                requireContext(),
                getString(
                    R.string.upload_failed,
                    state.errorType.code.toString(),
                    state.errorType::class.java.simpleName
                ),
                Toast.LENGTH_LONG
            ).show()

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

        mViewModel = ViewModelProvider(this).get(PinFromSmsViewModel::class.java)

        mViewModel.observableState.observe(viewLifecycleOwner, stateObserver)
    }
}