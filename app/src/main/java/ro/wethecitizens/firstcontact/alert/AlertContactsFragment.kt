package ro.wethecitizens.firstcontact.alert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_alert_others.view.*
import pub.devrel.easypermissions.EasyPermissions
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.camera.startScanner
import ro.wethecitizens.firstcontact.utils.InternetUtils
import ro.wethecitizens.firstcontact.utils.PermissionUtils

class AlertContactsFragment : Fragment(R.layout.fragment_alert_others), EasyPermissions.PermissionCallbacks {

    private lateinit var mViewModel: AlertContactsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(AlertContactsViewModel::class.java)

        view.camera_permission_box.isChecked = PermissionUtils.hasCameraPermission(view.context)
        view.internet_connection_box.isChecked = InternetUtils.hasInternetConnection(view.context)

        view.alert_button.setOnClickListener {

            when {
                PermissionUtils.hasCameraPermission(view.context).not() -> requestCameraPermission()
                InternetUtils.hasInternetConnection(view.context).not() -> requestInternetConnection()
                else -> startScanner(this)
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        view?.camera_permission_box?.isChecked = true
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
        private const val PERMISSION_REQUEST_CAMERA = 222;
    }
}