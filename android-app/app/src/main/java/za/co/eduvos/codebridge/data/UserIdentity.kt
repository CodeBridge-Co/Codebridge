package za.co.eduvos.codebridge.data

import android.content.Context
import za.co.eduvos.codebridge.core.network.TokenManager
import za.co.eduvos.codebridge.core.security.SecurePreferences

/**
 * Holds who is signed in. The hash ID itself is produced by core.security.HashIdManager at login
 * (from the student number); this object only remembers the result.
 */
object UserIdentity {

    private const val KEY_HASH_ID = "user_hash_id"
    private const val KEY_LOCATION_PERMITTED = "location_permitted"

    fun save(context: Context, hashId: String) {
        SecurePreferences(context.applicationContext).putString(KEY_HASH_ID, hashId)
    }

    /** Signed-in hash ID, or null when nobody has logged in yet. */
    fun hashId(context: Context): String? =
        SecurePreferences(context.applicationContext).getString(KEY_HASH_ID, null)?.takeIf { it.isNotBlank() }

    /** Short form (first 8 chars) of the hash ID for display and local progress keys; "GUEST" when logged out. */
    fun displayId(context: Context): String = hashId(context)?.take(8)?.uppercase() ?: "GUEST"

    fun isLoggedIn(context: Context): Boolean = hashId(context) != null && !TokenManager.getToken().isNullOrEmpty()

    fun logout(context: Context) {
        SecurePreferences(context.applicationContext).remove(KEY_HASH_ID)
        TokenManager.clear()
    }

    fun setLocationPermitted(context: Context, permitted: Boolean) {
        SecurePreferences(context.applicationContext).putString(KEY_LOCATION_PERMITTED, permitted.toString())
    }

    fun isLocationPermitted(context: Context): Boolean =
        SecurePreferences(context.applicationContext).getString(KEY_LOCATION_PERMITTED, "false") == "true"
}
