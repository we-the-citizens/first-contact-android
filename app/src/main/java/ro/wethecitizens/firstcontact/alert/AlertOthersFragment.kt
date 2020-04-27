package ro.wethecitizens.firstcontact.alert

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_alert_others.view.*
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.camera.startScanner

class AlertOthersFragment : Fragment(R.layout.fragment_alert_others) {

    private lateinit var mViewModel: AlertOthersViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(AlertOthersViewModel::class.java)

        view.alert_button.setOnClickListener {
            startScanner(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            mViewModel.getScanInfo(
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            )
        }
    }
}