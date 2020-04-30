package ro.wethecitizens.firstcontact.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*
import ro.wethecitizens.firstcontact.Preference
import ro.wethecitizens.firstcontact.R

class VerifyCallerFragment : Fragment(R.layout.fragment_upload_verifycaller) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var handShakePin = Preference.getHandShakePin(view.context)
        verifyCallerFragmentVerificationCode.text = handShakePin

        verifyCallerFragmentActionButton.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_upload_fragment)
                .navigate(R.id.action_verifyCallerFragment_to_enterPinFragment)
        }
    }
}
