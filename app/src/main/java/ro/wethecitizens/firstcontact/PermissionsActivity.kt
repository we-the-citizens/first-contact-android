// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_permissions.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import ro.wethecitizens.firstcontact.preference.Preference
import ro.wethecitizens.firstcontact.utils.Utils
import ro.wethecitizens.firstcontact.logging.CentralLog

private const val REQUEST_ENABLE_BT = 123
private const val PERMISSION_REQUEST_ACCESS_LOCATION = 456
private const val BATTERY_OPTIMISER = 789

class PermissionsActivity : AppCompatActivity() {

    private var TAG: String = "OnBoardingActivity"
    private var bleSupported = false
    private var mIsOpenSetting = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_permissions)

        tvPermissionsMessage.movementMethod = ScrollingMovementMethod()

        btnPermissionsStart.setOnClickListener{
            enableBluetooth()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mIsOpenSetting) {
            Handler().postDelayed(Runnable { setupPermissionsAndSettings() }, 1000)
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    fun enableBluetooth() {
        CentralLog.d(TAG, "[enableBluetooth]")
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.let {
            if (it.isDisabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(
                    enableBtIntent,
                    REQUEST_ENABLE_BT
                )
            } else {
                setupPermissionsAndSettings()
            }
        }
    }

    @AfterPermissionGranted(PERMISSION_REQUEST_ACCESS_LOCATION)
    fun setupPermissionsAndSettings() {
        CentralLog.d(TAG, "[setupPermissionsAndSettings]")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var perms = Utils.getRequiredPermissions()

            if (EasyPermissions.hasPermissions(this, *perms)) {
                // Already have permission, do the thing
                initBluetooth()
                excludeFromBatteryOptimization()
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(
                    this, getString(R.string.permission_location_rationale),
                    PERMISSION_REQUEST_ACCESS_LOCATION, *perms
                )
            }
        } else {
            initBluetooth()
            navigateToNextPage()
        }
    }

    private fun initBluetooth() {
        checkBLESupport()
    }

    private fun checkBLESupport() {
        CentralLog.d(TAG, "[checkBLESupport] ")
        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported) {
            bleSupported = false
            Utils.stopBluetoothMonitoringService(this)
        } else {
            bleSupported = true
        }
    }

    private fun excludeFromBatteryOptimization() {
        CentralLog.d(TAG, "[excludeFromBatteryOptimization] ")
        val powerManager =
            this.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
        val packageName = this.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent =
                Utils.getBatteryOptimizerExemptionIntent(
                    packageName
                )

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                CentralLog.d(TAG, "Not on Battery Optimization whitelist")
                //check if there's any activity that can handle this
                if (Utils.canHandleIntent(
                        intent,
                        packageManager
                    )
                ) {
                    this.startActivityForResult(
                        intent,
                        BATTERY_OPTIMISER
                    )
                } else {
                    //no way of handling battery optimizer
                    navigateToNextPage()
                }
            } else {
                CentralLog.d(TAG, "On Battery Optimization whitelist")
                navigateToNextPage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        CentralLog.d(TAG, "requestCode $requestCode resultCode $resultCode")
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish()
                return
            } else {
                setupPermissionsAndSettings()
            }
        } else if (requestCode == BATTERY_OPTIMISER) {
            if (resultCode != Activity.RESULT_CANCELED) {

//                Utils.keepServicesInChineseDevices(this)
                Handler().postDelayed({
                    navigateToNextPage()
                }, 1000)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLog.d(TAG, "[onRequestPermissionsResult] requestCode $requestCode")
        when (requestCode) {
            PERMISSION_REQUEST_ACCESS_LOCATION -> {
                for (x in 0 until permissions.size) {
                    var permission = permissions[x]
                    if (grantResults[x] == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            var showRationale = shouldShowRequestPermissionRationale(permission)
                            if (!showRationale) {

                                // build alert dialog
                                val dialogBuilder = AlertDialog.Builder(this)
                                // set message of alert dialog
                                dialogBuilder.setMessage(getString(R.string.open_location_setting))
                                    // if the dialog is cancelable
                                    .setCancelable(false)
                                    // positive button text and action
                                    .setPositiveButton(
                                        getString(R.string.ok),
                                        DialogInterface.OnClickListener { dialog, id ->
                                            CentralLog.d(TAG, "user also CHECKED never ask again")
                                            mIsOpenSetting = true
                                            var intent =
                                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            var uri: Uri =
                                                Uri.fromParts("package", packageName, null)
                                            intent.data = uri
                                            startActivity(intent)

                                        })
                                    // negative button text and action
                                    .setNegativeButton(
                                        getString(R.string.cancel),
                                        DialogInterface.OnClickListener { dialog, id ->
                                            dialog.cancel()
                                        })

                                // create dialog box
                                val alert = dialogBuilder.create()

                                // show alert dialog
                                alert.show()

                            } else if (Manifest.permission.WRITE_CONTACTS.equals(permission)) {
                                CentralLog.d(TAG, "user did not CHECKED never ask again")
                            } else {
                                excludeFromBatteryOptimization()
                            }
                        }
                    } else if (grantResults[x] == PackageManager.PERMISSION_GRANTED) {
                        excludeFromBatteryOptimization()
                    }
                }
            }
        }
    }

    fun navigateToNextPage() {

        CentralLog.d(TAG, "Navigating to next page")

        Preference.putIsOnBoarded(this, true)

        startActivity(
            Intent(this, MainActivity::class.java)
        )
    }
}
