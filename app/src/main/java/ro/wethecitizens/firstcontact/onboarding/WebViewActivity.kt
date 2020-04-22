package ro.wethecitizens.firstcontact.onboarding

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.webview.*
import ro.wethecitizens.firstcontact.BuildConfig
import ro.wethecitizens.firstcontact.R
import ro.wethecitizens.firstcontact.logging.CentralLog

class WebViewActivity : FragmentActivity() {

    private val TAG = "WebViewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        webview.webViewClient = WebViewClient()
        webview.loadUrl(BuildConfig.PRIVACY_URL)

        val wbc: WebChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(w: WebView) {
                CentralLog.d(TAG, "OnCloseWindow for WebChromeClient")
            }
        }

        webview.webChromeClient = wbc
    }
}
