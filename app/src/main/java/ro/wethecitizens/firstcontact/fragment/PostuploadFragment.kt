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
import kotlinx.android.synthetic.main.fragment_postupload.*
import kotlinx.android.synthetic.main.fragment_preupload.*
import kotlinx.android.synthetic.main.fragment_preupload.tvMessage
import ro.wethecitizens.firstcontact.MainActivity
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.preference.Preference

class PostuploadFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_postupload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvMessage.movementMethod = ScrollingMovementMethod()

        val isSent = Preference.isUploadSent(view.context)
        val isComplete = Preference.isUploadComplete(view.context)

        if (isComplete) {
            icon.setImageResource(R.drawable.done)
            tvMessage.setText(R.string.postupload_message_completed)
        }
        else if (isSent) {
            icon.setImageResource(R.drawable.time)
            tvMessage.setText(R.string.postupload_message_processing)
        }
    }
}
