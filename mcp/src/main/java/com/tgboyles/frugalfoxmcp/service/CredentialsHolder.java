package com.tgboyles.frugalfoxmcp.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

/**
 * Holds user credentials passed via SSE query parameters.
 * These credentials are used by TokenManager to automatically refresh JWT tokens.
 *
 * Security improvements:
 * - Passwords stored as char[] instead of String (can be zeroed from memory)
 * - Volatile fields for thread visibility
 * - Synchronized methods for thread safety
 * - Secure clearing with array zeroing
 */
@Service
public class CredentialsHolder {
    private volatile String username;
    private volatile char[] password;

    /**
     * Set credentials from SSE connection query parameters
     *
     * @param username The username
     * @param password The password (will be converted to char[] internally)
     */
    public synchronized void setCredentials(String username, String password) {
        // Clear existing password if present
        clearPasswordArray();

        this.username = username;
        this.password = password != null ? password.toCharArray() : null;
    }

    /**
     * Get the stored username
     */
    @NonNull
    public synchronized String getUsername() {
        return Objects.requireNonNull(username, "Username not set");
    }

    /**
     * Get a copy of the stored password
     * Caller is responsible for clearing the returned array after use
     *
     * @return A copy of the password as char array
     */
    @NonNull
    public synchronized char[] getPassword() {
        if (password == null) {
            throw new IllegalStateException("Password not set");
        }
        // Return a copy to prevent external modification
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        return Objects.requireNonNull(passwordCopy, "Password copy cannot be null");
    }

    /**
     * Check if credentials are available
     */
    public synchronized boolean hasCredentials() {
        return username != null && password != null && password.length > 0;
    }

    /**
     * Clear stored credentials securely
     * Zeros out the password array before dereferencing
     */
    public synchronized void clearCredentials() {
        this.username = null;
        clearPasswordArray();
    }

    /**
     * Helper method to securely zero out password array
     */
    private void clearPasswordArray() {
        if (password != null) {
            Arrays.fill(password, '\0');
            password = null;
        }
    }
}
