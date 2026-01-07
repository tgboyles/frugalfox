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
    void testSetAndGetCredentials() {
        String username = "testuser";
        String password = "testpass";

        credentialsHolder.setCredentials(username, password);

        assertEquals(username, credentialsHolder.getUsername());
        char[] retrievedPassword = credentialsHolder.getPassword();
        assertArrayEquals(password.toCharArray(), retrievedPassword);

        // Clean up
        Arrays.fill(retrievedPassword, '\0');
    }

    @Test
    void testHasCredentials() {
        assertFalse(credentialsHolder.hasCredentials());

        credentialsHolder.setCredentials("user", "pass");
        assertTrue(credentialsHolder.hasCredentials());

        credentialsHolder.clearCredentials();
        assertFalse(credentialsHolder.hasCredentials());
    }

    @Test
    void testClearCredentials() {
        credentialsHolder.setCredentials("testuser", "testpass");
        assertTrue(credentialsHolder.hasCredentials());

        credentialsHolder.clearCredentials();
        assertFalse(credentialsHolder.hasCredentials());

        // Verify accessing credentials after clear throws exception
        assertThrows(NullPointerException.class, () -> credentialsHolder.getUsername());
        assertThrows(IllegalStateException.class, () -> credentialsHolder.getPassword());
    }

    @Test
    void testPasswordIsCopied() {
        String password = "testpass";
        credentialsHolder.setCredentials("user", password);

        char[] password1 = credentialsHolder.getPassword();
        char[] password2 = credentialsHolder.getPassword();

        // Verify we get two different array instances (copies)
        assertNotSame(password1, password2);
        assertArrayEquals(password1, password2);

        // Modify one copy - should not affect the other
        Arrays.fill(password1, '\0');
        assertFalse(Arrays.equals(password1, password2));

        // Clean up
        Arrays.fill(password2, '\0');
    }

    @Test
    void testSetCredentialsReplacesOld() {
        credentialsHolder.setCredentials("user1", "pass1");
        char[] firstPassword = credentialsHolder.getPassword();

        // Set new credentials
        credentialsHolder.setCredentials("user2", "pass2");

        assertEquals("user2", credentialsHolder.getUsername());
        char[] secondPassword = credentialsHolder.getPassword();
        assertArrayEquals("pass2".toCharArray(), secondPassword);

        // Clean up
        Arrays.fill(firstPassword, '\0');
        Arrays.fill(secondPassword, '\0');
    }

    @Test
    void testNullPasswordHandling() {
        credentialsHolder.setCredentials("user", null);
        assertFalse(credentialsHolder.hasCredentials());
    }

    @Test
    void testEmptyPasswordHandling() {
        credentialsHolder.setCredentials("user", "");
        // Empty password should result in hasCredentials returning false
        assertFalse(credentialsHolder.hasCredentials());
    }
}
