package ro.wethecitizens.firstcontact.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_upload_foruse.*
import ro.wethecitizens.firstcontact.R

class ForUseByOTCFragment : Fragment(R.layout.fragment_forusebyotc) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        forUseFragmentActionButton.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_forUseByOTCFragment_to_uploadPageFragment)
        }
    }
}
