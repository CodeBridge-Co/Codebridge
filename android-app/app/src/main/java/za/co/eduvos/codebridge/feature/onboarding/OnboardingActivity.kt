package za.co.eduvos.codebridge.feature.onboarding

import android.content.Intent
import za.co.eduvos.codebridge.feature.auth.AuthActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply two types of green to CodeBridge logo ("Code" in dark green, "Bridge" in bright green)
        val darkGreen = ContextCompat.getColor(this, R.color.cb_dark_green)
        val brightGreen = ContextCompat.getColor(this, R.color.cb_green)

        binding.tvBrandTwoGreen.text = SpannableString("CodeBridge").apply {
            setSpan(ForegroundColorSpan(darkGreen), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(brightGreen), 4, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.btnGetStarted.setOnClickListener {
            // Sign in with student number + institution
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}
