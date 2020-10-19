// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import pub.devrel.easypermissions.EasyPermissions
import ro.wethecitizens.firstcontact.BuildConfig
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.fragment.alert.server.DocumentRequest
import ro.wethecitizens.firstcontact.fragment.alert.server.PositiveIdsRequest
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.temp_id_db.TempIdStorage
import ro.wethecitizens.firstcontact.utils.InternetUtils
import ro.wethecitizens.firstcontact.utils.PermissionUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class VerifyCallerFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    val PICK_IMAGE = 1000
    val REQUEST_TAKE_PHOTO = 1001
    lateinit var currentPhotoPath: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_verifycaller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verifyCallerTakePhotoFragmentActionButton.setOnClickListener {

            when {
                PermissionUtils.hasCameraPermission(view.context).not() -> requestCameraPermission()
                InternetUtils.hasInternetConnection(view.context).not() -> requestInternetConnection()
                else -> dispatchTakePictureIntent()
            }
        }

        verifyCallerChoosePhotoFragmentActionButton.setOnClickListener {
//            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
//            getIntent.type = "image/*"
//
//            val pickIntent = Intent(
//                Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            )
//            pickIntent.type = "image/*"
//
//            val chooserIntent = Intent.createChooser(getIntent, "Select Image")
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
//
//            startActivityForResult(chooserIntent, PICK_IMAGE)
                pickImage()
        }
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        dispatchTakePictureIntent()
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

        if (requestCode == PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            val uri = data?.data
            if (uri != null) {

 //               uploadFile(currentPhotoPath)
                val selectedImageUri = data.data

                if (selectedImageUri != null) {

                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor =
                        context?.contentResolver?.query(uri, filePathColumn, null, null, null)
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                            val filePath = cursor.getString(columnIndex)
                            cursor.close()
                            uploadFile(filePath)
                        }
                        cursor.close()
                    }
                    return
                }

            }
//            val bitmap : Bitmap? = data?.getExtras()?.getParcelable("data")
////            if(bitmap!= null){
////                uploadFile(bitmap);
////            }

        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            context?.getExternalFilesDir(currentPhotoPath)
            uploadFile(currentPhotoPath)
        }

//        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            val result = CropImage.getActivityResult(data)
//            if (resultCode === RESULT_OK) {
//                val resultUri = result.uri
//            } else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                val error = result.error
//            }
//        }

    }

//    fun getRealPathFromURI(
//        context: Context,
//        contentUri: Uri?
//    ): String? {
//        var cursor: Cursor? = null
//        return try {
//            val proj =
//                arrayOf(MediaStore.Images.Media.DATA)
//            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
//            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            cursor.moveToFirst()
//            cursor.getString(column_index)
//        } finally {
//            cursor?.close()
//        }
//    }


//    private fun uploadDoc(){
//        var file : File? = context?.let { createImageFile(it) }
//
////        var filePart : MultipartBody.Part = MultipartBody.Part.createFormData("file",
////            file?.name, RequestBody.create("image/*".toMediaTypeOrNull(), file));
//
//        val requestBody = file?.asRequestBody(file.extension.toMediaTypeOrNull())
//        val filePart = requestBody?.let {
//            MultipartBody.Part.createFormData(
//                "blob", file?.name, it
//            )
//        }
//
//        try {
//            val response = BackendMethods.getInstance().uploadDocument(filePart)
//
////            when (response.isSuccessful) {
////                true -> mState.postValue(PinFromSmsViewModel.State.IdsUploaded)
////                else -> {
////                    val errorCode = response.code()
////
////                    val errorType = HttpCode.getType(errorCode)
////
////                    mState.postValue(PinFromSmsViewModel.State.UploadFailed(errorType))
////                }
////            }
//        } catch (e: Exception) {
////            mState.postValue(PinFromSmsViewModel.State.UploadFailed(HttpCode.UNKNOWN_ERROR(-1)))
//            e.printStackTrace()
//        }
//    }

    fun uploadFile(fileUrl: String){
        val tempIdStorage = context?.let { TempIdStorage(it) }
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

        }

    }

    private fun pickImage() {
        if (context?.let { ActivityCompat.checkSelfPermission(it, READ_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            intent.putExtra("crop", "true")
            intent.putExtra("scale", true)
            intent.putExtra("aspectX", 16)
            intent.putExtra("aspectY", 9)
            startActivityForResult(intent, PICK_IMAGE)
            createImageFile(context!!)
        }
    }


    /* Private */

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
        private const val TAG = "VerifyCallerFragment"
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            context?.packageManager?.let {
                takePictureIntent.resolveActivity(it)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile(context!!)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            context!!,
                            BuildConfig.APPLICATION_ID + ".fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                    }
                }
            }
        }
    }

    private suspend fun getPositiveIdsList(tis: TempIdStorage) : List<PositiveIdsRequest.PositiveId> {

        val list: MutableList<PositiveIdsRequest.PositiveId> = mutableListOf()

        for (r in tis.getAllRecords()) {

            val c = Calendar.getInstance()
            c.timeInMillis = r.timestamp

            list.add(
                PositiveIdsRequest.PositiveId(
                tempId = r.v,
                date = Utils.formatCalendarToISO8601String(c)
            ))
        }

        return list.toList()
    }

    @Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}
