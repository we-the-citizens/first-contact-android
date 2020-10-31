// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.MediaColumns
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.fragment.alert.server.DocumentRequest
import ro.wethecitizens.firstcontact.fragment.alert.server.PositiveIdsRequest
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.temp_id_db.TempIdStorage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


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
            (parentFragment as UploadPageFragment).navigateToUploadPin(selectedImage)
        }
    }

    companion object {
        //private const val PERMISSION_REQUEST_CAMERA = 222
        private const val REQUEST_IMAGE_GET = 1
        private const val TAG = "VerifyCallerFragment"
    }
}
