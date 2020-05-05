package ro.wethecitizens.firstcontact.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.main_activity_onboarding.*
import ro.wethecitizens.firstcontact.R

class PreOnboardingActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_onboarding)
        btn_onboardingStart.setOnClickListener {
            var intent = Intent(this, OnBoardingActivity::class.java)
            startActivity(intent)
        }
    }
}
