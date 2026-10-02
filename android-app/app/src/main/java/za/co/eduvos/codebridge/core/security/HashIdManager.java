package za.co.eduvos.codebridge.core.security;

import android.content.Context;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class HashIdManager {

    private static final String PREF_SALT_KEY = "hash_salt";
    private final SecurePreferences securePreferences;

    public HashIdManager(Context context) {
        this.securePreferences = new SecurePreferences(context);
    }

    // Package-private constructor for unit testing
    HashIdManager(SecurePreferences securePreferences) {
        this.securePreferences = securePreferences;
    }

    public String generateHashId(String studentNumber) {
        try {
            String salt = getOrGenerateSalt();
            String saltedInput = salt + studentNumber;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(saltedInput.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate hash ID", e);
        }
    }

    private String getOrGenerateSalt() {
        String existingSalt = securePreferences.getString(PREF_SALT_KEY, null);
        if (existingSalt != null) return existingSalt;

        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        String newSalt = Base64.encodeToString(saltBytes, Base64.NO_WRAP);
        securePreferences.putString(PREF_SALT_KEY, newSalt);
        return newSalt;
    }
}
