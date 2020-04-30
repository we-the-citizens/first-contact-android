package ro.wethecitizens.firstcontact.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_upload_page.*
import ro.wethecitizens.firstcontact.BuildConfig
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.status.persistence.StatusRecord
import ro.wethecitizens.firstcontact.streetpass.persistence.StreetPassRecord

data class ExportData(val recordList: List<StreetPassRecord>, val statusList: List<StatusRecord>)

class UploadPageFragment : Fragment(R.layout.fragment_upload_page) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var uuidString = BuildConfig.BLE_SSID

        uuidString = uuidString.substring(uuidString.length - 4)
        fragment_buildno.text = "${BuildConfig.GITHASH}-${uuidString}"
    }

    fun turnOnLoadingProgress() {
        uploadPageFragmentLoadingProgressBarFrame.visibility = View.VISIBLE
    }

    fun turnOffLoadingProgress() {
        uploadPageFragmentLoadingProgressBarFrame.visibility = View.INVISIBLE
    }

    fun navigateToUploadComplete() {
        Navigation.findNavController(requireActivity(), R.id.nav_upload_fragment)
            .navigate(R.id.action_enterPinFragment_to_uploadCompleteFragment)
    }

    fun popStack() {
        Navigation.findNavController(requireActivity(), R.id.nav_upload_fragment)
            .popBackStack()
    }
}
