package za.co.eduvos.codebridge.data

import android.content.Context

/**
 * The database uses a number for the session id, but the app uses a UUID. This turns the current
 * UUID into the same number every time. 0 means no session has started.
 */
object SessionIds {
    fun localId(context: Context): Int {
        val uuid = context.getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE)
            .getString("current_session_id", null) ?: return 0
        return uuid.hashCode() and Int.MAX_VALUE
    }
}
