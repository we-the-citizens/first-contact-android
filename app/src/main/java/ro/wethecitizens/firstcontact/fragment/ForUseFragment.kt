// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.fragment_upload_foruse.*
import ro.wethecitizens.firstcontact.preference.Preference
import ro.wethecitizens.firstcontact.R

class ForUseFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_foruse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isComplete = Preference.isUploadComplete(view.context)

        if (isComplete) {

            tv_anonimization.isVisible = false
            tapNextText.setText(R.string.upload_for_use_tap_next_text_2)
            forUseFragmentActionButton.setText(R.string.upload_for_use_next_btn_2)

            forUseFragmentActionButton.isEnabled = false
            forUseFragmentActionButton.alpha = 0.7f
        }
        else {

            forUseFragmentActionButton.isEnabled = true

            forUseFragmentActionButton.setOnClickListener {
                (parentFragment as ForUseByOTCFragment).goToUploadFragment()
            }

            tv_anonimization.isVisible = true
            tv_anonimization.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Firebase.remoteConfig.getString("anonimization_url")))
                startActivity(browserIntent)
            }
        }
    }
}
