// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.FragmentActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.activity_onboarding.*
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.services.FirebaseService

class OnboardingActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        FirebaseService.initFirebase(this)

        btn_onboardingStart.setOnClickListener {
            var intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
        }

        tvLink.setOnClickListener {
            val url = Firebase.remoteConfig.getString("how_it_works_url")
            if (url != null && url.length > 0) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        }
        tvOnboardingMessage.movementMethod = ScrollingMovementMethod()
    }
}
