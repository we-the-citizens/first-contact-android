package ro.wethecitizens.firstcontact.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_upload_enterpin.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.TracerApp
import ro.wethecitizens.firstcontact.Utils
import ro.wethecitizens.firstcontact.status.persistence.StatusRecord
import ro.wethecitizens.firstcontact.status.persistence.StatusRecordStorage
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecordStorage

class EnterPinFragment : Fragment() {
    private var TAG = "UploadFragment"

    private var disposeObj: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_enterpin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterPinFragmentUploadCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.length == 6) {
                    Utils.hideKeyboardFrom(view.context, view)
                }
            }
        })

        enterPinActionButton.setOnClickListener {
            enterPinFragmentErrorMessage.visibility = View.INVISIBLE
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.turnOnLoadingProgress()

            var observableStreetRecords = Observable.create<List<StreetPassRecord>> {
                val result = StreetPassRecordStorage(TracerApp.AppContext).getAllRecords()
                it.onNext(result)
            }
            var observableStatusRecords = Observable.create<List<StatusRecord>> {
                val result = StatusRecordStorage(TracerApp.AppContext).getAllRecords()
                it.onNext(result)
            }

            disposeObj = Observable.zip(observableStreetRecords, observableStatusRecords,

                BiFunction<List<StreetPassRecord>, List<StatusRecord>, ExportData> { records, status ->
                    ExportData(
                        records,
                        status
                    )
                }

            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe { exportedData ->
                    Log.d(TAG, "records: ${exportedData.recordList}")
                    Log.d(TAG, "status: ${exportedData.statusList}")
                }
        }

        enterPinFragmentBackButtonLayout.setOnClickListener {
            println("onclick is pressed")
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.popStack()
        }

        enterPinFragmentBackButton.setOnClickListener {
            println("onclick is pressed")
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.popStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposeObj?.dispose()
    }
}
