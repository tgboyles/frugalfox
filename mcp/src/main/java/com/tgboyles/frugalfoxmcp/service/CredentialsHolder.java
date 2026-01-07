package com.tgboyles.frugalfoxmcp.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Holds user credentials passed via SSE query parameters.
 * These credentials are used by TokenManager to automatically refresh JWT tokens.
 */
@Service
public class CredentialsHolder {
    private String username;
    private String password;

    /**
     * Set credentials from SSE connection query parameters
     */
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Get the stored username
     */
    @NonNull
    public String getUsername() {
        return Objects.requireNonNull(username, "Username not set");
    }

    /**
     * Get the stored password
     */
    @NonNull
    public String getPassword() {
        return Objects.requireNonNull(password, "Password not set");
    }

    /**
     * Check if credentials are available
     */
    public boolean hasCredentials() {
        return username != null && password != null;
    }

    /**
     * Clear stored credentials
     */
    public void clearCredentials() {
        this.username = null;
        this.password = null;
    }
}
