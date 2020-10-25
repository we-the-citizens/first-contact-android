// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*
import pub.devrel.easypermissions.EasyPermissions
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.camera.startScanner
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.utils.InternetUtils
import ro.wethecitizens.firstcontact.utils.PermissionUtils


class VerifyCallerFragment : Fragment()/*, EasyPermissions.PermissionCallbacks*/ {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_verifycaller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verifyCallerFragmentActionButton.setOnClickListener {

            /*when {
                PermissionUtils.hasCameraPermission(view.context).not() -> requestCameraPermission()
                InternetUtils.hasInternetConnection(view.context).not() -> requestInternetConnection()
                else -> startScanner(this)
            }*/

            pickImage();
        }
    }

    private fun pickImage() {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            //if (intent.resolveActivity(activity?.packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            //}
    }

    /*override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
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
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            //val thumbnail: Bitmap = data.getParcelableExtra("data")
            val selectedImageUri  = data?.data

            if (selectedImageUri  != null) {
                uploadFile(selectedImageUri)
            }
        }

        /*if (requestCode == IntentIntegrator.REQUEST_CODE) {

            val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

            if (result == null) {
                CentralLog.d(TAG, "onActivityResult result null")
            }
            else {

                val qrCode = result.contents

                if (qrCode == null) {
                    CentralLog.d(TAG, "onActivityResult scan did not return anything")
                }
                else {
                    (parentFragment as UploadPageFragment).navigateToUploadPin(qrCode)
                }
            }
        }*/
    }

    fun uploadFile(fileUrl: Uri){
        /*val tempIdStorage = context?.let { TempIdStorage(it) }
        val file = File(fileUrl)
//        val date = Date();
//        val positiveId: DocumentRequest.PositiveId = DocumentRequest.PositiveId(TempIdStorage , date)
//
//        val positiveIdList = listOf<DocumentRequest.PositiveId>(positiveId)

        val requestBody = file.asRequestBody(file.extension.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            "document",file.name,requestBody
        )
        lifecycleScope.launch {

            val docModel = DocumentRequest(data =getPositiveIdsList(tempIdStorage!!))
            if(docModel.data.isEmpty()){
                Toast.makeText(context , "Nu exista TempID !" , Toast.LENGTH_LONG).show()
            } else {
                BackendMethods.getInstance().uploadDocument(filePart, docModel)
            }

        }*/

    }



    /* Private */
    /*private fun requestCameraPermission() {
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.permission_camera_rationale),
            PERMISSION_REQUEST_CAMERA,
            *PermissionUtils.cameraRequiredPermissions()
        )
    }

    private fun requestInternetConnection() {
        Toast.makeText(requireContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
    }*/

    companion object {
        //private const val PERMISSION_REQUEST_CAMERA = 222
        private const val REQUEST_IMAGE_GET = 1
        private const val TAG = "VerifyCallerFragment"
    }
}
