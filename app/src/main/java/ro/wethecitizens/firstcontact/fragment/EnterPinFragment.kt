package ro.wethecitizens.firstcontact.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_upload_enterpin.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.fragment.alert.AlertContactsViewModel
import ro.wethecitizens.firstcontact.fragment.alert.PinFromSmsViewModel
import ro.wethecitizens.firstcontact.fragment.alert.SmsListenerViewModel
import java.util.*
import kotlin.concurrent.schedule

class EnterPinFragment(private val inQRCode: String) : Fragment() {

    //private var TAG = "UploadFragment"

    private var disposeObj: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_enterpin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appCtx: Context = view.context


        alertContactsViewModel = ViewModelProvider(this).get(AlertContactsViewModel::class.java)
        alertContactsViewModel.observableState.observe(viewLifecycleOwner, alertContactsObserver)


        mViewModel = ViewModelProvider(this).get(PinFromSmsViewModel::class.java)
        mViewModel.observableState.observe(viewLifecycleOwner, stateObserver)

        mSharedViewModel = ViewModelProvider(requireActivity()).get(SmsListenerViewModel::class.java)
        mSharedViewModel.observableState.observe(viewLifecycleOwner, smsObserver)
        mSharedViewModel.smsText.observe(viewLifecycleOwner, Observer { sms ->
            mViewModel.handleSms(sms, appCtx)
        })


        enterPinActionButton.setOnClickListener {
            alertContactsViewModel.setQRCode(inQRCode)
        }

        enterPinFragmentBackButtonLayout.setOnClickListener {
            (parentFragment as UploadPageFragment).popStack()
        }

        enterPinFragmentBackButton.setOnClickListener {
            (parentFragment as UploadPageFragment).popStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeObj?.dispose()
    }



    /* Private */

    private lateinit var mSharedViewModel: SmsListenerViewModel
    private val smsObserver = Observer<SmsListenerViewModel.State> { state ->
        when (state) {
            SmsListenerViewModel.State.ListeningForSms -> {
            }
            else -> {
                //Toast.makeText(requireContext(), R.string.invalid_sms, Toast.LENGTH_LONG).show()
            }
        }
    }

    private lateinit var mViewModel: PinFromSmsViewModel
    private val stateObserver = Observer<PinFromSmsViewModel.State> { state ->
        when (state) {
            PinFromSmsViewModel.State.InvalidSms ->
                Toast.makeText(requireContext(), R.string.invalid_sms, Toast.LENGTH_LONG).show()

            is PinFromSmsViewModel.State.ValidSms -> {

//                hideLoader()
//
//                enterPinFragmentUploadCode?.setText(state.pin)
            }

            is PinFromSmsViewModel.State.UploadFailed -> {

                hideLoader()

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

            PinFromSmsViewModel.State.IdsUploaded -> {

                Timer("CompleteDelayed", false).schedule(100) {

                    hideLoader()

                    val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
                    pf.navigateToUploadComplete()
                }
            }
        }
    }

    private lateinit var alertContactsViewModel: AlertContactsViewModel
    private val alertContactsObserver = Observer<AlertContactsViewModel.State> { state ->

        when (state) {

            is AlertContactsViewModel.State.Loading -> {
                showLoader()
                startSmsListener(state.qrCode)
            }

            is AlertContactsViewModel.State.Success -> {
                hideLoader()
//                Timer("UploadPinDelayed", false).schedule(300) {
//
//                    val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
//                    pf.navigateToUploadPin()
//                }
            }
            is AlertContactsViewModel.State.Failed -> {
                hideLoader()
                Toast.makeText(
                    requireContext(),
                    "Error code: ${state.errorType.code}, ${state.errorType::class.java.simpleName}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    /**
     * Starts the SMS Retriever flow before doing the authorization request due to SMS being received too fast.
     */
    private fun startSmsListener(qrCode: String) {

        // retrieve SMS client from activity context
        val client: SmsRetrieverClient = SmsRetriever.getClient(requireActivity())

        // get shared view model
        ViewModelProvider(requireActivity()).get(SmsListenerViewModel::class.java)
            .also { sharedViewModel ->

                sharedViewModel.observableState.observe(
                    viewLifecycleOwner,
                    Observer { state ->
                        // listen for the SMS Retriever callback to do the authorization request
                        state?.let { alertContactsViewModel.checkAuthorization(qrCode) }
                    }
                )

                // start SMS retriever
                sharedViewModel.listenForSms(client)
            }
    }


    private fun showLoader() {

        val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
        pf.turnOnLoadingProgress()
    }

    private fun hideLoader() {

        val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
        pf.turnOffLoadingProgress()
    }
}
