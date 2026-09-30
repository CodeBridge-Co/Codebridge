package za.co.eduvos.codebridge.core.network;
import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "codebridge_prefs";
    private static final String KEY_JWT = "jwt_token";
    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveToken(String token) {
        prefs.edit().putString(KEY_JWT, token).apply();
    }

    public static String getToken() {
        return prefs != null ? prefs.getString(KEY_JWT, null) : null;
    }

    public static void clear() {
        prefs.edit().remove(KEY_JWT).apply();
    }
}

// For Token manager initialization
package za.co.eduvos.codebridge;

import android.app.Application;
import za.co.eduvos.codebridge.core.network.TokenManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TokenManager.init(this);
    }
}
