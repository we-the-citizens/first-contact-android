// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment.alert

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.wethecitizens.firstcontact.Preference
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.fragment.alert.server.PositiveIdsRequest
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.server.BackendMethods
import ro.wethecitizens.firstcontact.server.HttpCode
import ro.wethecitizens.firstcontact.temp_id_db.TempIdStorage
import ro.wethecitizens.firstcontact.utils.SingleLiveEvent
import java.util.*
import java.util.regex.Pattern

class PinFromSmsViewModel : ViewModel() {

    private val mState: SingleLiveEvent<State> = SingleLiveEvent()
    val observableState: LiveData<State> = mState

    fun handleSms(smsContent: String?, appCtx: Context) {
        when (smsContent) {
            null -> mState.value = State.InvalidSms
            else -> {
                retrievePinFromSms(smsContent)?.let { pin ->
                    mState.value = State.ValidSms
                    uploadContacts(pin, appCtx)
                } ?: run {
                    mState.value = State.InvalidSms
                }
            }
        }
    }

    private fun uploadContacts(pinCode: String, appCtx: Context) {

        CentralLog.d(TAG, "PIN $pinCode  uploadContacts")

        val tempIdStorage = TempIdStorage(appCtx)

        viewModelScope.launch(Dispatchers.IO) {
            val pacientId = Preference.getPatientIdQr()!!

            val request = PositiveIdsRequest(
                pacientId,
                pinCode,

//                // FIXME: Load from database!!!
//                listOf(
//                    PositiveIdsRequest.PositiveId(
//                        tempId = "TEST_10_2_10",
//                        date = "2020-04-21T00:11:22.333Z"
//                    )
//                )
                getPositiveIdsList(tempIdStorage)
            )

            try {
                val response = BackendMethods.getInstance().uploadPositiveIds(request)

                when (response.isSuccessful) {
                    true -> mState.postValue(State.IdsUploaded)
                    else -> {
                        val errorCode = response.code()

                        val errorType = HttpCode.getType(errorCode)

                        mState.postValue(State.UploadFailed(errorType))
                    }
                }
            } catch (e: Exception) {
                mState.postValue(State.UploadFailed(HttpCode.UNKNOWN_ERROR(-1)))
                e.printStackTrace()
            }
        }
    }

    private suspend fun getPositiveIdsList(tis: TempIdStorage) : List<PositiveIdsRequest.PositiveId> {

        val list: MutableList<PositiveIdsRequest.PositiveId> = mutableListOf()

        for (r in tis.getAllRecords()) {

            val c = Calendar.getInstance()
            c.timeInMillis = r.timestamp

            list.add(PositiveIdsRequest.PositiveId(
                tempId = r.v,
                date = Utils.formatCalendarToISO8601String(c)
            ))
        }

        return list.toList()
    }

    /**
     * PIN Code should be the first number in the sms string.
     */
    fun retrievePinFromSms(smsContent: String): String? {
        return Pattern.compile("\\d+")
            .matcher(smsContent)
            .takeIf { it.find() }
            ?.group(0)
    }

    sealed class State {
        object InvalidSms : State()
        object ValidSms : State()

        object IdsUploaded : State()
        class UploadFailed(val errorType: HttpCode) : State()
    }

    companion object {
        const val TAG = "PinFromSmsViewModel"
    }
}