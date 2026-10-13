package za.co.eduvos.codebridge.feature.onboarding

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.ActivityHashIdGeneratedBinding

class HashIdGeneratedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHashIdGeneratedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHashIdGeneratedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve or generate the user's persistent Hash ID (e.g., CB7F4A2D9E)
        val hashId = UserIdentity.displayId(this)
        binding.tvGeneratedHashId.text = hashId

        binding.btnCopyHashId.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("CodeBridge Hash ID", hashId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Hash ID copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        binding.btnGoToDashboard.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
