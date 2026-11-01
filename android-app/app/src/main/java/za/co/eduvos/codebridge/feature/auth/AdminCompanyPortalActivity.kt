package za.co.eduvos.codebridge.feature.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient // <--- Added Import
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import za.co.eduvos.codebridge.BuildConfig
import za.co.eduvos.codebridge.R

class AdminCompanyPortalActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_company_portal)

        val webView = findViewById<WebView>(R.id.webView)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        
        val customCss = """
            javascript:(function() {
                var style = document.createElement('style');
                style.innerHTML = '.topbar { display: none !important; } body { padding: 10px; }';
                document.head.appendChild(style);
            })()
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl(customCss) 
            }
        }
        
        webView.loadUrl("${BuildConfig.BASE_URL}docs")

        btnLogout.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java) 
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
