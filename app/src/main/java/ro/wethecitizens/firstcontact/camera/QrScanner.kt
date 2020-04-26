package ro.wethecitizens.firstcontact.camera

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import ro.wethecitizens.firstcontact.R

/**
 * Catch camera response by overriding [AppCompatActivity.onActivityResult] and calling
 * [IntentIntegrator.parseActivityResult] when request code is [IntentIntegrator.REQUEST_CODE].
 */
fun startScanner(activity: AppCompatActivity) {
    val intent = IntentIntegrator(activity)

    prepareIntent(intent, activity.baseContext)

    intent.initiateScan()
}

/**
 * Catch camera response by overriding [Fragment.onActivityResult] and calling
 * [IntentIntegrator.parseActivityResult] when request code is [IntentIntegrator.REQUEST_CODE].
 *
 * Make sure that the activity is passing the result to the fragment by calling [super.onActivityResult]
 * inside [AppCompatActivity.onActivityResult]. Otherwise listen inside activity.
 */
fun startScanner(fragment: Fragment) {
    val intent = IntentIntegrator.forSupportFragment(fragment)

    prepareIntent(intent, fragment.requireContext())

    intent.initiateScan()
}

private fun prepareIntent(intent: IntentIntegrator, context: Context) {
    intent.apply {
//        setDesiredBarcodeFormats() // FIXME: change when type is known
        setPrompt(context.getString(R.string.camera_scan_message)) // FIXME: change text
        setCameraId(0) // FIXME: check back camera
        setBeepEnabled(false)
        setBarcodeImageEnabled(false)
    }
}