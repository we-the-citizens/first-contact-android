package ro.wethecitizens.firstcontact.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_upload_enterpin.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.fragment.alert.PinFromSmsViewModel
import ro.wethecitizens.firstcontact.fragment.alert.SmsListenerViewModel
import java.util.*
import kotlin.concurrent.schedule

class EnterPinFragment : Fragment() {

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

        mViewModel = ViewModelProvider(this).get(PinFromSmsViewModel::class.java)

        mViewModel.observableState.observe(viewLifecycleOwner, stateObserver)

        mSharedViewModel =
            ViewModelProvider(requireActivity()).get(SmsListenerViewModel::class.java)

        mSharedViewModel.observableState.observe(viewLifecycleOwner, smsObserver)

        mSharedViewModel.smsText.observe(viewLifecycleOwner, Observer { sms ->
            mViewModel.handleSms(sms, appCtx)
        })


        enterPinFragmentUploadCode.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.length == 6) {
                    Utils.hideKeyboardFrom(view.context, view)
                }
            }
        })

        enterPinActionButton.setOnClickListener {

            enterPinFragmentUploadCode.text.takeIf { it.isNotEmpty() }?.let {

                enterPinFragmentErrorMessage.visibility = View.INVISIBLE

                showLoader()

                mViewModel.uploadContacts(it.toString(), appCtx)

            } ?: run {

                enterPinFragmentErrorMessage.visibility = View.VISIBLE
            }
        }

        enterPinFragmentBackButtonLayout.setOnClickListener {

            val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
            pf.popStack()
        }

        enterPinFragmentBackButton.setOnClickListener {

            val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
            pf.popStack()
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
                enterPinFragmentUploadCode?.setHint(R.string.listening_for_sms)
            }
            else -> {
                enterPinFragmentUploadCode?.setHint(R.string.pin_from_sms)
            }
        }
    }

    private lateinit var mViewModel: PinFromSmsViewModel
    private val stateObserver = Observer<PinFromSmsViewModel.State> { state ->
        when (state) {
            PinFromSmsViewModel.State.InvalidSms -> enterPinFragmentUploadCode?.setHint(R.string.pin_from_sms)
            is PinFromSmsViewModel.State.ValidSms -> {

                hideLoader()

                enterPinFragmentUploadCode?.setText(state.pin)
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

    private fun showLoader() {

        val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
        pf.turnOnLoadingProgress()
    }

    private fun hideLoader() {

        val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
        pf.turnOffLoadingProgress()
    }
}
