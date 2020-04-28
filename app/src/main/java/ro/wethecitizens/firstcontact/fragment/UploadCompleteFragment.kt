package ro.wethecitizens.firstcontact.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_upload_uploadcomplete.*
import ro.wethecitizens.firstcontact.R

class UploadCompleteFragment : Fragment(R.layout.fragment_upload_uploadcomplete) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uploadCompleteFragmentActionButton.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .popBackStack(R.id.homeFragment, false)
        }
    }
}
