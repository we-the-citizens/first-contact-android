package ro.wethecitizens.firstcontact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main_new.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_new)

        Utils.startBluetoothMonitoringService(this)
        Utils.startPeriodicallyDownloadService(this)


        nav_view.setupWithNavController(Navigation.findNavController(this, R.id.nav_host_fragment))
    }
}
