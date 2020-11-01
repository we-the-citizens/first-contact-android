// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_upload_select_image.*
import ro.wethecitizens.firstcontact.R


class SelectImageFragment : Fragment()/*, EasyPermissions.PermissionCallbacks*/ {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_select_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verifyCallerFragmentActionButton.setOnClickListener {
            pickImage();
        }
    }

    private fun pickImage() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_IMAGE_GET)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            val selectedImage = data?.data
            (parentFragment as UploadPageFragment).navigateToConfirmUpload(selectedImage)
        }
    }

    companion object {
        //private const val PERMISSION_REQUEST_CAMERA = 222
        private const val REQUEST_IMAGE_GET = 1
        private const val TAG = "VerifyCallerFragment"
    }
}
