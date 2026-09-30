package za.co.eduvos.codebridge.core.security;

import android.content.Context;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

public class HashIdManager {

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "CodeBridgeHashKey";
    private static final String PREF_SALT_KEY = "hash_salt";

    private final SecurePreferences securePreferences;

    public HashIdManager(Context context) {
        this.securePreferences = new SecurePreferences(context);
    }

    /**
     * Generates a salted SHA-256 hash ID for a student.
     * @param studentNumber The raw student number.
     * @return Base64 encoded SHA-256 hash.
     */
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

    private String getOrGenerateSalt() throws Exception {
        String existingSalt = securePreferences.getString(PREF_SALT_KEY, null);
        if (existingSalt != null) {
            return existingSalt;
        }

        // Generate new salt
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        String newSalt = Base64.encodeToString(saltBytes, Base64.NO_WRAP);

        // Secure it in EncryptedSharedPreferences (which uses Android Keystore internally)
        securePreferences.putString(PREF_SALT_KEY, newSalt);
        return newSalt;
    }
}
