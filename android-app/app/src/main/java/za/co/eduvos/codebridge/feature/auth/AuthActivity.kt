package za.co.eduvos.codebridge.feature.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.eduvos.codebridge.R

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .commit()
        }
    }
}
