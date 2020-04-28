package ro.wethecitizens.firstcontact

import android.app.Application
import android.content.Context
import android.os.Build
import ro.wethecitizens.firstcontact.idmanager.TempIDManager
import ro.wethecitizens.firstcontact.logging.CentralLog
import ro.wethecitizens.firstcontact.services.BluetoothMonitoringService
import ro.wethecitizens.firstcontact.streetpass.CentralDevice
import ro.wethecitizens.firstcontact.streetpass.PeripheralDevice
import java.util.*

class TracerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext = applicationContext

        saveInstallDate()
    }

    private fun saveInstallDate() {

        val ts = Preference.getInstallDateTS(AppContext)

        if (ts.compareTo(0) == 0) {

            Preference.putInstallDateTS(
                AppContext,
                Calendar.getInstance().timeInMillis
            )
        }
    }


    companion object {

        private val TAG = "TracerApp"
        const val ORG = BuildConfig.ORG

        lateinit var AppContext: Context

        fun thisDeviceMsg(): String {
            BluetoothMonitoringService.broadcastMessage?.let {
                CentralLog.i(TAG, "Retrieved BM for storage: $it")

                if (!it.isValidForCurrentTime()) {

                    var fetch = TempIDManager.retrieveTemporaryID(AppContext)
                    fetch?.let {
                        CentralLog.i(TAG, "Grab New Temp ID")
                        BluetoothMonitoringService.broadcastMessage = it
                    }

                    if (fetch == null) {
                        CentralLog.e(TAG, "Failed to grab new Temp ID")
                    }

                }
            }
            return BluetoothMonitoringService.broadcastMessage?.tempID ?: "Missing TempID"
        }

        fun asPeripheralDevice(): PeripheralDevice {
            return PeripheralDevice(Build.MODEL, "SELF")
        }

        fun asCentralDevice(): CentralDevice {
            return CentralDevice(Build.MODEL, "SELF")
        }
    }
}
