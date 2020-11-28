// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_onboarding.*
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
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ConfirmUploadFragment() : Fragment() {

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

        val selectedImage: Uri? = Uri.parse(this.arguments?.getString("selectedImage"))

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

        tvStep2Message.movementMethod = ScrollingMovementMethod()
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

        try {

            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedImage!!, "r", null) ?: return

            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(context?.cacheDir, getFileName(selectedImage!!))
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            val requestBody = file.asRequestBody(file.extension.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "document", file.name, requestBody
            )

            val tempIdStorage = context?.let {
                TempIdStorage(
                    it
                )
            }

            lifecycleScope.launch {

                val positiveIdsList = getPositiveIdsList(tempIdStorage!!)
                try {
                    val docModel =
                        DocumentRequest(
                            data = positiveIdsList,
                            signature = getSignature(positiveIdsList)
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
                                hideLoader()
                                val errorCode = response.code()
                                val errorType = HttpCode.getType(errorCode)
                                if (errorCode == 403)
                                    Toast.makeText(context, R.string.error_document_already_uploaded, Toast.LENGTH_LONG).show()
                                else
                                    Toast.makeText(context, "Eroare la upload:" + errorType.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Eroare interna", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }

            }
        }
        catch (e: Exception) {
            Toast.makeText(context, "Eroare deschidere fisier", Toast.LENGTH_LONG).show()
            e.printStackTrace()
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

    private fun getSignature(list: List<PositiveIdsRequest.PositiveId>): String
    {
        var data:String = ""
        for (l in list)
            data += l.tempId + l.date

        //Log.i(TAG, "DATA: " + data);

        val algoType: String = "H" + "m" + "a" + "c" + "S" + "H" + "A" + "2" + "5" + "6"
        val mac = Mac.getInstance(algoType)
        val key = Firebase.remoteConfig.getString("signature_key")
        val secretKey = SecretKeySpec(key.toByteArray(), algoType)
        mac.init(secretKey)

        //Log.i(TAG, "KEY: " + key);

        var signature = Base64.encodeToString(mac.doFinal(data.toByteArray()), Base64.DEFAULT)
        signature = signature.trimEnd()
        //Log.i(TAG, "SIGNATURE: " + signature);
        return signature
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
        private const val TAG = "ConfirmUploadFragment"
    }
}
