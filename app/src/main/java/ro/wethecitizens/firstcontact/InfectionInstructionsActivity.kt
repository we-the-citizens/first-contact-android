// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact

import android.net.http.SslError
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.activity_infection_instructions.*


class InfectionInstructionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infection_instructions)

        try {

            CookieManager.getInstance().setAcceptCookie(true)


            val ws = webView.settings

            ws.javaScriptEnabled = true
            ws.domStorageEnabled = true
            ws.setAppCacheEnabled(true)
            ws.setSupportMultipleWindows(false)
            ws.javaScriptCanOpenWindowsAutomatically = false
            ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW


            webView.webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    url: String
                ): Boolean {
                    return false
                }

                override fun onReceivedSslError(
                    view: WebView,
                    handler: SslErrorHandler,
                    error: SslError
                ) {
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                }
            }



            webView.loadUrl(url)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }


    val url:String = Firebase.remoteConfig.getString("infection_instructions_url")

}
