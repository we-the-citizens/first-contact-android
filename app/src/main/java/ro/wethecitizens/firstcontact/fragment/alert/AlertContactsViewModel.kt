// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment.alert

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ro.wethecitizens.firstcontact.Preference
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.fragment.alert.server.AuthorizationRequest
import ro.wethecitizens.firstcontact.fragment.alert.server.DocumentRequest
import ro.wethecitizens.firstcontact.fragment.alert.server.PositiveIdsRequest
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.server.HttpCode
import ro.wethecitizens.firstcontact.temp_id_db.TempIdStorage
import ro.wethecitizens.firstcontact.utils.AppSignatureHelper
import ro.wethecitizens.firstcontact.utils.SingleLiveEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

class AlertContactsViewModel : ViewModel() {

    private val mState: SingleLiveEvent<State> = SingleLiveEvent()
    val observableState: LiveData<State> = mState

    fun setSelectedImage(selectedImage: Uri?) {

        mState.value = State.Loading(selectedImage)
    }

    sealed class State {
        class Loading(val selectedImage: Uri?) : State()
        object Success : State()
        class Failed(val errorType: HttpCode) : State()
    }
}
