package za.co.eduvos.codebridge;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import za.co.eduvos.codebridge.core.security.HashIdManager;
import za.co.eduvos.codebridge.core.security.SecurePreferences;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HashIdManagerTest {

    @Mock
    private SecurePreferences mockSecurePreferences;

    private HashIdManager hashIdManager;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Mock the salt retrieval
        when(mockSecurePreferences.getString("hash_salt", null)).thenReturn("test_salt_123");
        hashIdManager = new HashIdManager(mockSecurePreferences);
    }

    @Test
    public void testGenerateHashId_ReturnsConsistentHash() {
        String studentNumber = "STU12345";
        String hash1 = hashIdManager.generateHashId(studentNumber);
        String hash2 = hashIdManager.generateHashId(studentNumber);

        assertNotNull(hash1);
        assertEquals(hash1, hash2); // Same input + salt = same hash
    }

    @Test
    public void testGenerateHashId_DifferentInputsReturnDifferentHashes() {
        String hash1 = hashIdManager.generateHashId("STU12345");
        String hash2 = hashIdManager.generateHashId("STU67890");

        assertNotEquals(hash1, hash2);
    }
}
