// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_upload_confirm.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.utils.Utils
import ro.wethecitizens.firstcontact.server.DocumentRequest
import ro.wethecitizens.firstcontact.server.PositiveIdsRequest
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.server.HttpCode
import ro.wethecitizens.firstcontact.idmanager.persistence.TempIdStorage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

class ConfirmUploadFragment(private val selectedImage: Uri?) : Fragment() {

    private var disposeObj: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterPinActionButton.setOnClickListener {
            showLoader()
            CentralLog.d(TAG, "Uploading image and keys")
            uploadImage(selectedImage);
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


    private fun uploadImage(selectedImage:Uri?) {
        if (selectedImage == null) {
            Toast.makeText(context, "Selectati o imagine valida", Toast.LENGTH_LONG).show()
            return
        }

        val contentResolver = context?.contentResolver
        if (contentResolver == null) {
            Toast.makeText(context, "Eroare interna", Toast.LENGTH_SHORT).show()
            return
        }

        val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedImage!!, "r", null) ?: return

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(context?.cacheDir,  getFileName(selectedImage!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        val requestBody = file.asRequestBody(file.extension.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            "document",file.name,requestBody
        )

        val tempIdStorage = context?.let {
            TempIdStorage(
                it
            )
        }

        lifecycleScope.launch {

            try {
                val docModel =
                    DocumentRequest(
                        data = getPositiveIdsList(tempIdStorage!!)
                    )
                if (docModel.data.isEmpty()) {
                    Toast.makeText(context, "Nu exista ID-uri anonime!", Toast.LENGTH_LONG).show()
                } else {
                    val response = BackendMethods.getInstance().uploadDocument(filePart, docModel)

                    when (response.isSuccessful) {
                        true -> {
                            hideLoader()
                            (parentFragment as UploadPageFragment).navigateToUploadComplete()
                        }

                        else -> {
                            val errorCode = response.code()
                            val errorType = HttpCode.getType(errorCode)
                            Toast.makeText(context, "Eroare la upload:" + errorType.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Eroare interna", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
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

    fun getFileName(fileUri: Uri): String {
        var name = ""
        val returnCursor = context?.contentResolver?.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return name
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
