// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*
import ro.wethecitizens.firstcontact.fragment.PreuploadFragment
import ro.wethecitizens.firstcontact.fragment.HomeFragment
import ro.wethecitizens.firstcontact.fragment.PostuploadFragment
import ro.wethecitizens.firstcontact.preference.Preference
import ro.wethecitizens.firstcontact.services.FirebaseService
import ro.wethecitizens.firstcontact.utils.Utils

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    // navigation
    private var mNavigationLevel = 0
    var LAYOUT_MAIN_ID = 0
    private var selected = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Utils.startBluetoothMonitoringService(this)
        FirebaseService.initFirebase(this)

        LAYOUT_MAIN_ID = R.id.content

        val mOnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {

                        if (selected != R.id.navigation_home) {
                            openFragment(
                                LAYOUT_MAIN_ID, HomeFragment(),
                                HomeFragment::class.java.name, 0
                            )
                        }
                        selected = R.id.navigation_home
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_upload -> {
                        if (selected != R.id.navigation_upload) {

                            if (Preference.isUploadSent(this) || Preference.isUploadComplete(this))
                                openFragment(
                                    LAYOUT_MAIN_ID, PostuploadFragment(),
                                    PostuploadFragment::class.java.name, 0
                                )
                            else
                                openFragment(
                                    LAYOUT_MAIN_ID, PreuploadFragment(),
                                    PreuploadFragment::class.java.name, 0
                                )
                        }

                        selected = R.id.navigation_upload
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_help -> {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Firebase.remoteConfig.getString("help_url")))
                        startActivity(browserIntent)
                        //Toast.makeText(this, "To be implemented", Toast.LENGTH_LONG).show()
                    }
                }
                false
            }

        nav_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        goToHome()
    }


    fun goToHome() {
        nav_view.selectedItemId = R.id.navigation_home
    }

    fun openFragment(
        containerViewId: Int,
        fragment: Fragment,
        tag: String,
        title: Int
    ) {
        try { // pop all fragments
            supportFragmentManager.popBackStackImmediate(
                LAYOUT_MAIN_ID,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            mNavigationLevel = 0
            val transaction =
                supportFragmentManager.beginTransaction()
            transaction.replace(containerViewId, fragment, tag)
            transaction.commit()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
