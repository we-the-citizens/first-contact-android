// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

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
import ro.wethecitizens.firstcontact.logging.CentralLog

class EnterPinFragment(private val inQRCode: String) : Fragment() {

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

            showLoader()

            CentralLog.d(TAG, "QR Code = $inQRCode   enterPinActionButton.setOnClickListener ")

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
            PinFromSmsViewModel.State.InvalidSms -> {
                hideLoader()
                Toast.makeText(requireContext(), R.string.invalid_sms, Toast.LENGTH_LONG).show()
            }

            is PinFromSmsViewModel.State.ValidSms -> {
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
                hideLoader()
                (parentFragment as UploadPageFragment).navigateToUploadComplete()
            }
        }
    }

    private lateinit var alertContactsViewModel: AlertContactsViewModel
    private val alertContactsObserver = Observer<AlertContactsViewModel.State> { state ->

        when (state) {

            is AlertContactsViewModel.State.Loading -> {
                startSmsListener(state.qrCode)
            }

            is AlertContactsViewModel.State.Success -> {
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

        CentralLog.d(TAG, "QR Code = $inQRCode   startSmsListener")

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

        (parentFragment as UploadPageFragment).turnOnLoadingProgress()
    }

    private fun hideLoader() {

        (parentFragment as UploadPageFragment).turnOffLoadingProgress()
    }


    companion object {
        private const val TAG = "EnterPinFragment"
    }
}
