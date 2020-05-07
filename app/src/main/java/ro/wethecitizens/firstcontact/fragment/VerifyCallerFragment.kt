package ro.wethecitizens.firstcontact.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_alert_others.view.*
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*
import pub.devrel.easypermissions.EasyPermissions
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.camera.startScanner
import ro.wethecitizens.firstcontact.fragment.alert.AlertContactsViewModel
import ro.wethecitizens.firstcontact.fragment.alert.SmsListenerViewModel
import ro.wethecitizens.firstcontact.utils.InternetUtils
import ro.wethecitizens.firstcontact.utils.PermissionUtils


class VerifyCallerFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_verifycaller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        verifyCallerFragmentActionButton.setOnClickListener {
//            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
//            myParentFragment.navigateToUploadPin()
//        }


        mViewModel = ViewModelProvider(this).get(AlertContactsViewModel::class.java)
        mViewModel.observableState.observe(viewLifecycleOwner, stateObserver)

//        view.camera_permission_box.isChecked = PermissionUtils.hasCameraPermission(view.context)
//        view.internet_connection_box.isChecked = InternetUtils.hasInternetConnection(view.context)

        verifyCallerFragmentActionButton.setOnClickListener {

            when {
                PermissionUtils.hasCameraPermission(view.context).not() -> requestCameraPermission()
                InternetUtils.hasInternetConnection(view.context).not() -> requestInternetConnection()
                else -> startScanner(this)
            }
        }
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

        startScanner(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            mViewModel.getScanInfo(
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            )
        }
    }



    /* Private */

    private lateinit var mViewModel: AlertContactsViewModel
    private val stateObserver = Observer<AlertContactsViewModel.State> { state ->
        view?.loading_layout?.visibility = when (state) {
            is AlertContactsViewModel.State.Loading -> View.VISIBLE
            else -> View.GONE
        }

        when (state) {
            is AlertContactsViewModel.State.Loading -> startSmsListener(state.qrCode)
            is AlertContactsViewModel.State.Success -> {
                val pf: UploadPageFragment = (parentFragment as UploadPageFragment)
                pf.navigateToUploadPin()
            }
            is AlertContactsViewModel.State.Failed -> {
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
                        state?.let { mViewModel.checkAuthorization(qrCode) }
                    }
                )

                // start SMS retriever
                sharedViewModel.listenForSms(client)
            }
    }


    private fun requestCameraPermission() {
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.permission_camera_rationale),
            PERMISSION_REQUEST_CAMERA,
            *PermissionUtils.cameraRequiredPermissions()
        )
    }

    private fun requestInternetConnection() {
        Toast.makeText(requireContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PERMISSION_REQUEST_CAMERA = 222
    }
}
