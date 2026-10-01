package za.co.eduvos.codebridge.core.security;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import net.sqlcipher.database.SupportFactory;

public class SqlCipherManager {

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "CodeBridgeDBKey";

    /**
     * Provides the SupportFactory for Room with the SQLCipher passphrase.
     */
    public static SupportFactory getSupportFactory(Context context) {
        try {
            byte[] passphrase = getOrGenerateKey(context);
            return new SupportFactory(passphrase);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLCipher", e);
        }
    }

    private static byte[] getOrGenerateKey(Context context) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build());
            keyGenerator.generateKey();
        }

        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        SecretKey secretKey = secretKeyEntry.getSecretKey();
        
        // Return the encoded key bytes to be used as SQLCipher passphrase
        return secretKey.getEncoded();
    }
}
