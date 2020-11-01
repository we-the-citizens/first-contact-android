// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_onboarding.*

class OnboardingActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        btn_onboardingStart.setOnClickListener {
            var intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
        }

        tvMessage.movementMethod = ScrollingMovementMethod()
    }
}
