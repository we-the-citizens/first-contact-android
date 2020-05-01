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

            val c = Calendar.getInstance()

            //20 zile inapoi
            c.timeInMillis = c.timeInMillis - (20 * 24 * 60 * 60 * 1000)

            CentralLog.w(TAG, c.toString())

            Preference.putInstallDateTS(
                AppContext,
                c.timeInMillis
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

                    var fetch = TempIDManager.retrieveNewTemporaryID(AppContext)
                    fetch?.let {
                        CentralLog.i(TAG, "Grab New Temp ID")
                        BluetoothMonitoringService.broadcastMessage = it
                    }

                    if (fetch == null) {
                        CentralLog.e(TAG, "Failed to grab new Temp ID")
                    }

                }
            }


            CentralLog.i(TAG, "thisDeviceMsg: ${BluetoothMonitoringService.broadcastMessage?.tempID}")


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
