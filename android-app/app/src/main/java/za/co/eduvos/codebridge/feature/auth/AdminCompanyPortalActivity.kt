package za.co.eduvos.codebridge.feature.auth // Or whatever your UI package is

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.common.NetworkConfig // Assuming you have your Base URL here

class AdminCompanyPortalActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_company_portal)

        val webView = findViewById<WebView>(R.id.webView)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Enable JavaScript for Swagger UI to work properly
        webView.settings.javaScriptEnabled = true
        
        // Load the Swagger docs using your Retrofit Base URL
        // E.g., "http://10.0.2.2:8000/docs"
        webView.loadUrl("${NetworkConfig.BASE_URL}docs")

        btnLogout.setOnClickListener {
            // Clear session and go back to login
            val intent = Intent(this, AuthActivity::class.java) // Use your actual Auth/Login Activity class here
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
