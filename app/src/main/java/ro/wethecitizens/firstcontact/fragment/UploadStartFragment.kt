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
import kotlinx.android.synthetic.main.fragment_upload_start.*
import ro.wethecitizens.firstcontact.preference.Preference
import ro.wethecitizens.firstcontact.R

class UploadStartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isComplete = Preference.isUploadComplete(view.context)

        if (isComplete) {

            tv_anonimization.isVisible = false
            tapNextText.setText(R.string.preupload_message_disabled)
            forUseFragmentActionButton.setText(R.string.preupload_go_button_disabled)

            forUseFragmentActionButton.isEnabled = false
            forUseFragmentActionButton.alpha = 0.7f
        }
        else {

            forUseFragmentActionButton.isEnabled = true

            forUseFragmentActionButton.setOnClickListener {
                (parentFragment as UploadWrapperFragment).goToUploadFragment()
            }

            tv_anonimization.isVisible = true
            tv_anonimization.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Firebase.remoteConfig.getString("anonimization_url")))
                startActivity(browserIntent)
            }
        }
    }
}