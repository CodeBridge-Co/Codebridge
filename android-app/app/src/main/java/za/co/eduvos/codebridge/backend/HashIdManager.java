package za.co.eduvos.codebridge.backend;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Java Manager for Unique Device Hash ID System.
 * Generates an 8-character uppercase random Hash ID for each device/installation (e.g., 7F3A9C2B).
 */
public class HashIdManager {

    private static final String PREF_NAME = "codebridge_user_session";
    private static final String KEY_HASH_ID = "user_hash_id";
    private static final String KEY_LOCATION_PERMITTED = "location_permitted";

    private static String currentHashId = null;

    public static synchronized boolean hasHashId(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_HASH_ID, null);
        return saved != null && !saved.trim().isEmpty();
    }

    public static synchronized String getOrGenerateHashId(Context context) {
        if (currentHashId != null && !currentHashId.isEmpty()) {
            return currentHashId;
        }

        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String savedHashId = prefs.getString(KEY_HASH_ID, null);

        if (savedHashId == null || savedHashId.trim().isEmpty()) {
            // Generate a unique 8-character uppercase hex Hash ID for this device (e.g., 7F3A9C2B)
            savedHashId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            prefs.edit().putString(KEY_HASH_ID, savedHashId).apply();
        }

        currentHashId = savedHashId;
        return currentHashId;
    }

    public static void setLocationPermitted(Context context, boolean permitted) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LOCATION_PERMITTED, permitted).apply();
    }

    public static boolean isLocationPermitted(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOCATION_PERMITTED, false);
    }

    public static void resetHashId(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_HASH_ID).apply();
        currentHashId = null;
    }
}
