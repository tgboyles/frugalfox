package com.tgboyles.frugalfoxmcp.service;

import com.tgboyles.frugalfoxmcp.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages JWT tokens with automatic expiration checking and refresh.
 * Tokens are cached in memory and automatically refreshed when expired.
 */
@Service
public class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);

    private final FrugalFoxApiClient apiClient;
    private final ConcurrentHashMap<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();

    public TokenManager(FrugalFoxApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get a valid token for the given username/password.
     * Returns cached token if valid, otherwise refreshes.
     */
    @NonNull
    public String getValidToken(@NonNull String username, @NonNull String password) {
        String cacheKey = username;
        TokenInfo cachedToken = tokenCache.get(cacheKey);

        // Check if we have a valid cached token
        if (cachedToken != null && !isTokenExpired(cachedToken.token)) {
            log.debug("Using cached token for user: {}", username);
            return cachedToken.token;
        }

        // Token expired or doesn't exist - login to get new token
        log.info("Token expired or missing for user: {}, refreshing...", username);
        AuthResponse authResponse = apiClient.login(username, password);

        TokenInfo newTokenInfo = new TokenInfo(authResponse.token(), username, password);
        tokenCache.put(cacheKey, newTokenInfo);

        log.info("Token refreshed successfully for user: {}", username);
        return authResponse.token();
    }

    /**
     * Check if a JWT token is expired or about to expire (within 60 seconds).
     *
     * JWT tokens have three parts separated by dots: header.payload.signature
     * The payload is base64 encoded and contains the expiration claim.
     */
    private boolean isTokenExpired(String token) {
        try {
            // Split JWT into parts
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid JWT format - expected 3 parts, got {}", parts.length);
                return true;
            }

            // Decode payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Extract exp claim (expiration timestamp in seconds)
            // Simple JSON parsing - look for "exp":timestamp
            int expIndex = payload.indexOf("\"exp\":");
            if (expIndex == -1) {
                log.warn("No exp claim found in JWT payload");
                return true;
            }

            // Extract the number after "exp":
            String expSection = payload.substring(expIndex + 6); // Skip "exp":
            int commaIndex = expSection.indexOf(',');
            int braceIndex = expSection.indexOf('}');
            int endIndex = (commaIndex > 0 && commaIndex < braceIndex) ? commaIndex : braceIndex;

            if (endIndex == -1) {
                log.warn("Could not parse exp claim from JWT");
                return true;
            }

            long expiration = Long.parseLong(expSection.substring(0, endIndex).trim());
            long currentTime = Instant.now().getEpochSecond();

            // Consider token expired if it expires within the next 60 seconds (buffer)
            boolean expired = (expiration - currentTime) < 60;

            if (expired) {
                log.debug("Token expired or expiring soon. Exp: {}, Current: {}, Diff: {}s",
                    expiration, currentTime, (expiration - currentTime));
            }

            return expired;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            // If we can't parse the token, consider it expired to force refresh
            return true;
        }
    }

    /**
     * Clear all cached tokens (useful for testing or logout scenarios)
     */
    public void clearCache() {
        tokenCache.clear();
        log.info("Token cache cleared");
    }

    /**
     * Remove token for specific user
     */
    public void clearToken(String username) {
        tokenCache.remove(username);
        log.info("Token cleared for user: {}", username);
    }

    /**
     * Internal class to store token info
     */
    private static class TokenInfo {
        @NonNull
        final String token;

        TokenInfo(@NonNull String token, @NonNull String username, @NonNull String password) {
            this.token = token;
        }
    }
}
