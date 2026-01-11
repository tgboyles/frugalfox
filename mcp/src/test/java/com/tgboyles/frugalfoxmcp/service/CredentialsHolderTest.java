package com.tgboyles.frugalfoxmcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CredentialsHolder to verify security improvements:
 * - Passwords stored as char[] instead of String
 * - Thread safety with synchronized methods
 * - Secure clearing with array zeroing
 */
class CredentialsHolderTest {

    private CredentialsHolder credentialsHolder;

    @BeforeEach
    void setUp() {
        credentialsHolder = new CredentialsHolder();
    }

    @Test
    void setCredentials_ValidUsernameAndPassword_StoresAndRetrievesCorrectly() {
        // Arrange
        String username = "testuser";
        String password = "testpass";

        // Act
        credentialsHolder.setCredentials(username, password);

        // Assert
        assertEquals(username, credentialsHolder.getUsername());
        char[] retrievedPassword = credentialsHolder.getPassword();
        assertArrayEquals(password.toCharArray(), retrievedPassword);

        // Clean up
        Arrays.fill(retrievedPassword, '\0');
    }

    @Test
    void hasCredentials_NoCredentialsSet_ReturnsFalse() {
        // Arrange - credentialsHolder initialized in setUp()

        // Act
        boolean result = credentialsHolder.hasCredentials();

        // Assert
        assertFalse(result);
    }

    @Test
    void hasCredentials_CredentialsSet_ReturnsTrue() {
        // Arrange
        credentialsHolder.setCredentials("user", "pass");

        // Act
        boolean result = credentialsHolder.hasCredentials();

        // Assert
        assertTrue(result);
    }

    @Test
    void hasCredentials_CredentialsCleared_ReturnsFalse() {
        // Arrange
        credentialsHolder.setCredentials("user", "pass");
        credentialsHolder.clearCredentials();

        // Act
        boolean result = credentialsHolder.hasCredentials();

        // Assert
        assertFalse(result);
    }

    @Test
    void clearCredentials_CredentialsExist_ClearsSuccessfully() {
        // Arrange
        credentialsHolder.setCredentials("testuser", "testpass");

        // Act
        credentialsHolder.clearCredentials();

        // Assert
        assertFalse(credentialsHolder.hasCredentials());
    }

    @Test
    void getUsername_AfterClear_ThrowsNullPointerException() {
        // Arrange
        credentialsHolder.setCredentials("testuser", "testpass");
        credentialsHolder.clearCredentials();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> credentialsHolder.getUsername());
    }

    @Test
    void getPassword_AfterClear_ThrowsIllegalStateException() {
        // Arrange
        credentialsHolder.setCredentials("testuser", "testpass");
        credentialsHolder.clearCredentials();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> credentialsHolder.getPassword());
    }

    @Test
    void getPassword_CalledMultipleTimes_ReturnsDifferentInstances() {
        // Arrange
        String password = "testpass";
        credentialsHolder.setCredentials("user", password);

        // Act
        char[] password1 = credentialsHolder.getPassword();
        char[] password2 = credentialsHolder.getPassword();

        // Assert - Verify we get two different array instances (copies)
        assertNotSame(password1, password2);
        assertArrayEquals(password1, password2);

        // Clean up
        Arrays.fill(password1, '\0');
        Arrays.fill(password2, '\0');
    }

    @Test
    void getPassword_ModifyingReturnedCopy_DoesNotAffectStoredPassword() {
        // Arrange
        String password = "testpass";
        credentialsHolder.setCredentials("user", password);
        char[] password1 = credentialsHolder.getPassword();

        // Act - Modify one copy
        Arrays.fill(password1, '\0');

        // Assert - Should not affect the stored password or new copies
        char[] password2 = credentialsHolder.getPassword();
        assertFalse(Arrays.equals(password1, password2));
        assertArrayEquals("testpass".toCharArray(), password2);

        // Clean up
        Arrays.fill(password2, '\0');
    }

    @Test
    void setCredentials_CalledTwice_ReplacesOldCredentials() {
        // Arrange
        credentialsHolder.setCredentials("user1", "pass1");

        // Act
        credentialsHolder.setCredentials("user2", "pass2");

        // Assert
        assertEquals("user2", credentialsHolder.getUsername());
        char[] secondPassword = credentialsHolder.getPassword();
        assertArrayEquals("pass2".toCharArray(), secondPassword);

        // Clean up
        Arrays.fill(secondPassword, '\0');
    }

    @Test
    void setCredentials_NullPassword_ResultsInNoCredentials() {
        // Arrange & Act
        credentialsHolder.setCredentials("user", null);

        // Assert
        assertFalse(credentialsHolder.hasCredentials());
    }

    @Test
    void setCredentials_EmptyPassword_ResultsInNoCredentials() {
        // Arrange & Act
        credentialsHolder.setCredentials("user", "");

        // Assert
        assertFalse(credentialsHolder.hasCredentials());
    }
}
