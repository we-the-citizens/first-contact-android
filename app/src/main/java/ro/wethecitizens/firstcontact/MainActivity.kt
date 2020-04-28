package ro.wethecitizens.firstcontact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main_new.*
import ro.wethecitizens.firstcontact.logging.CentralLog

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        Utils.startBluetoothMonitoringService(this)

        nav_view.setupWithNavController(Navigation.findNavController(this, R.id.nav_host_fragment))

        getFCMToken()
    }

    private fun getFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (!task.isSuccessful()) {
                    CentralLog.w(TAG, "failed to get fcm token ${task.exception}")
                    return@addOnCompleteListener
                } else {
                    // Get new Instance ID token
                    val token = task.result?.token
                    // Log and toast
                    CentralLog.d(TAG, "FCM token: $token")
                }
            }
    }
}
