package za.co.eduvos.codebridge.feature.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.feature.auth.AuthActivity
import za.co.eduvos.codebridge.feature.auth.CompanyDashboardActivity
import za.co.eduvos.codebridge.feature.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val dark = ContextCompat.getColor(this, R.color.cb_dark_green)
        val green = ContextCompat.getColor(this, R.color.cb_green)

        findViewById<TextView>(R.id.tvBrand).text = SpannableString("CodeBridge").apply {
            setSpan(ForegroundColorSpan(dark), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(green), 4, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE)
            val role = prefs.getString("user_role", "STUDENT")
            val isLoggedIn = UserIdentity.isLoggedIn(this)

            if (isLoggedIn) {
                if (role == "COMPANY") {
                    startActivity(Intent(this, CompanyDashboardActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            } else {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            finish()
        }, 1800)
    }
}