package com.tgboyles.frugalfoxmcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Holds user credentials passed via SSE query parameters.
 * These credentials are used by TokenManager to automatically refresh JWT tokens.
 */
@Service
public class CredentialsHolder {

    private static final Logger log = LoggerFactory.getLogger(CredentialsHolder.class);

    private String username;
    private String password;

    /**
     * Set credentials from SSE connection query parameters
     */
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        log.info("Credentials set for user: {}", username);
    }

    /**
     * Get the stored username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the stored password
     */
    public String getPassword() {
        return password;
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
        log.info("Credentials cleared");
    }
}
