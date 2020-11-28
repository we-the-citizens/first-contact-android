// Copyright (c) 2020 BlueTrace.io

package ro.wethecitizens.firstcontact.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.fragment_preupload.*
import ro.wethecitizens.firstcontact.MainActivity
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.logging.CentralLog

class PreuploadFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preupload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radio_no_document.setOnClickListener { view -> onRadioButtonClicked(view) }
        radio_document.setOnClickListener { view -> onRadioButtonClicked(view) }
        radio_document_anonimized.setOnClickListener { view -> onRadioButtonClicked(view) }
        radio_sms.setOnClickListener { view -> onRadioButtonClicked(view) }
        goButton.visibility = View.INVISIBLE
        tvMessage.movementMethod = ScrollingMovementMethod()
    }

    fun onRadioButtonClicked(view: View) {

        goButton.visibility = View.VISIBLE

        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_no_document ->
                    if (checked) {
                        tvMessage.text = getString(R.string.preupload_message_no_document)
                        goButton.text = getString(R.string.preupload_button_ok)
                        goButton.setOnClickListener { goBackHome() }
                    }
                R.id.radio_document ->
                    if (checked) {
                        tvMessage.text = getString(R.string.preupload_message_document)
                        goButton.text = getString(R.string.preupload_button_instructions)
                        goButton.setOnClickListener { openAnonimizationInstructions() }
                    }
                R.id.radio_document_anonimized ->
                    if (checked) {
                        tvMessage.text = getString(R.string.preupload_message_document_anonimized)
                        goButton.text = getString(R.string.preupload_button_warn_contacts)
                        goButton.setOnClickListener { goToUploadFragment() }
                    }
                R.id.radio_sms ->
                    if (checked) {
                        tvMessage.text = getString(R.string.preupload_message_sms)
                        goButton.text = getString(R.string.preupload_button_warn_contacts)
                        goButton.setOnClickListener { goToUploadFragment() }
                    }
            }
        }
    }

    fun goToUploadFragment() {
        val parentActivity: MainActivity = activity as MainActivity
        parentActivity.openFragment(
            parentActivity.LAYOUT_MAIN_ID,
            UploadPageFragment(),
            UploadPageFragment::class.java.name,
            0
        )
    }

    fun goBackHome() {
        val parentActivity: MainActivity = activity as MainActivity
        parentActivity.goToHome()
    }

    fun openAnonimizationInstructions() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Firebase.remoteConfig.getString("anonimization_url")))
        startActivity(browserIntent)
    }
}
