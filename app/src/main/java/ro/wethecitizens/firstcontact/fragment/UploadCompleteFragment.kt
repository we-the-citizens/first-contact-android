// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_upload_uploadcomplete.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.preference.Preference

class UploadCompleteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_uploadcomplete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Preference.putIsUploadSent(view.context, true)

        uploadCompleteFragmentActionButton.setOnClickListener {
            (parentFragment as UploadPageFragment).goBackToHome()
        }
    }
}
