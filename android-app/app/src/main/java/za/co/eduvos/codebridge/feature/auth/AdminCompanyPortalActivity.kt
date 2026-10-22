package za.co.eduvos.codebridge.feature.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
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

        // Enables JavaScript for Swagger UI to work properly
        webView.settings.javaScriptEnabled = true
        
        // BuildConfig.BASE_URL always ends with a "/" (required by Retrofit)
        // So this will correctly load "http://10.0.2.2:8000/docs"
        webView.loadUrl("${BuildConfig.BASE_URL}docs")

        btnLogout.setOnClickListener {
            // Route back to the AuthActivity
            val intent = Intent(this, AuthActivity::class.java)
            // Clear the backstack so the user can't press back to get into the portal again
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
