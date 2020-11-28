// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_document_viewer.*
import ro.wethecitizens.firstcontact.R


class ConfirmImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedImage: Uri? = Uri.parse(this.arguments?.getString("selectedImage"))
        documentView.setImageURI(selectedImage)

        confirmSelectionButton.setOnClickListener {
            confirmImage(selectedImage)
        }

        backButton.setOnClickListener {
            goBack()
        }
    }

    private fun confirmImage(selectedImage: Uri?) {
        (parentFragment as UploadPageFragment).navigateToConfirmUpload(selectedImage)
    }

    private fun goBack() {
        (parentFragment as UploadPageFragment).popStack()
    }

    companion object {
        private const val TAG = "ConfirmImageFragment"
    }
}
