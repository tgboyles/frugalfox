package com.tgboyles.frugalfoxmcp.service;

import com.tgboyles.frugalfoxmcp.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenManager to verify:
 * - Token caching and retrieval
 * - Token expiration detection
 * - Automatic token refresh
 * - Secure password handling
 */
@ExtendWith(MockitoExtension.class)
class TokenManagerTest {

    @Mock
    private FrugalFoxApiClient apiClient;

    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenManager = new TokenManager(apiClient);
    }

    @Test
    void getValidToken_NewUser_CallsApiAndCachesToken() {
        // Arrange
        String username = "testuser";
        char[] password = "testpass".toCharArray();
        String expectedToken = createValidToken(3600); // Token expires in 1 hour
        when(apiClient.login(eq(username), any(String.class)))
            .thenReturn(new AuthResponse(expectedToken, username, "test@example.com"));

        // Act
        String actualToken = tokenManager.getValidToken(username, password);

        // Assert
        assertEquals(expectedToken, actualToken);
        verify(apiClient, times(1)).login(eq(username), any(String.class));
        
        // Verify password was cleared
        assertTrue(isArrayCleared(password));
    }

    @Test
    void getValidToken_CachedValidToken_DoesNotCallApi() {
        // Arrange
        String username = "testuser";
        char[] password1 = "testpass".toCharArray();
        char[] password2 = "testpass".toCharArray();
        String expectedToken = createValidToken(3600); // Token expires in 1 hour
        when(apiClient.login(eq(username), any(String.class)))
            .thenReturn(new AuthResponse(expectedToken, username, "test@example.com"));

        // First call to cache the token
        tokenManager.getValidToken(username, password1);

        // Act - Second call should use cache
        String actualToken = tokenManager.getValidToken(username, password2);

        // Assert
        assertEquals(expectedToken, actualToken);
        verify(apiClient, times(1)).login(eq(username), any(String.class)); // Only called once
        
        // Verify both passwords were cleared
        assertTrue(isArrayCleared(password1));
        assertTrue(isArrayCleared(password2));
    }

    @Test
    void getValidToken_ExpiredToken_RefreshesToken() {
        // Arrange
        String username = "testuser";
        char[] password1 = "testpass".toCharArray();
        char[] password2 = "testpass".toCharArray();
        String expiredToken = createValidToken(-60); // Token expired 60 seconds ago
        String newToken = createValidToken(3600); // New token expires in 1 hour
        
        when(apiClient.login(eq(username), any(String.class)))
            .thenReturn(new AuthResponse(expiredToken, username, "test@example.com"))
            .thenReturn(new AuthResponse(newToken, username, "test@example.com"));

        // First call gets expired token
        tokenManager.getValidToken(username, password1);

        // Act - Second call should refresh
        String actualToken = tokenManager.getValidToken(username, password2);

        // Assert
        assertEquals(newToken, actualToken);
        verify(apiClient, times(2)).login(eq(username), any(String.class)); // Called twice
    }

    @Test
    void getValidToken_TokenExpiringInLessThan60Seconds_RefreshesToken() {
        // Arrange
        String username = "testuser";
        char[] password1 = "testpass".toCharArray();
        char[] password2 = "testpass".toCharArray();
        String soonToExpireToken = createValidToken(30); // Expires in 30 seconds
        String newToken = createValidToken(3600); // New token expires in 1 hour
        
        when(apiClient.login(eq(username), any(String.class)))
            .thenReturn(new AuthResponse(soonToExpireToken, username, "test@example.com"))
            .thenReturn(new AuthResponse(newToken, username, "test@example.com"));

        // First call gets token expiring soon
        tokenManager.getValidToken(username, password1);

        // Act - Second call should refresh (token within 60 second buffer)
        String actualToken = tokenManager.getValidToken(username, password2);

        // Assert
        assertEquals(newToken, actualToken);
        verify(apiClient, times(2)).login(eq(username), any(String.class));
    }

    @Test
    void clearCache_CachedTokens_RemovesAllTokens() {
        // Arrange
        String username = "testuser";
        char[] password1 = "testpass".toCharArray();
        char[] password2 = "testpass".toCharArray();
        String token = createValidToken(3600);
        when(apiClient.login(eq(username), any(String.class)))
            .thenReturn(new AuthResponse(token, username, "test@example.com"));

        // Cache a token
        tokenManager.getValidToken(username, password1);

        // Act
        tokenManager.clearCache();

        // Second call should hit API again
        tokenManager.getValidToken(username, password2);

        // Assert
        verify(apiClient, times(2)).login(eq(username), any(String.class));
    }

    @Test
    void clearToken_SpecificUser_RemovesOnlyThatToken() {
        // Arrange
        String username1 = "user1";
        String username2 = "user2";
        char[] password1 = "pass1".toCharArray();
        char[] password1Again = "pass1".toCharArray();
        char[] password2a = "pass2".toCharArray();
        char[] password2b = "pass2".toCharArray();
        String token1 = createValidToken(3600);
        String token2 = createValidToken(3600);
        
        when(apiClient.login(eq(username1), any(String.class)))
            .thenReturn(new AuthResponse(token1, username1, "user1@example.com"));
        when(apiClient.login(eq(username2), any(String.class)))
            .thenReturn(new AuthResponse(token2, username2, "user2@example.com"));

        // Cache tokens for both users
        tokenManager.getValidToken(username1, password1);
        tokenManager.getValidToken(username2, password2a);

        // Act - Clear only user2's token
        tokenManager.clearToken(username2);

        // Request tokens again
        tokenManager.getValidToken(username1, password1Again); // Should use cache
        tokenManager.getValidToken(username2, password2b); // Should call API

        // Assert
        verify(apiClient, times(1)).login(eq(username1), any(String.class)); // User1 only called once
        verify(apiClient, times(2)).login(eq(username2), any(String.class)); // User2 called twice
    }

    @Test
    void getValidToken_InvalidTokenFormat_RefreshesToken() {
        // Arrange
        String username = "testuser";
        char[] password1 = "invalidtoken".toCharArray();
        char[] password2 = "testpass".toCharArray();
        String invalidToken = "not.a.valid.jwt"; // Invalid JWT format
        String validToken = createValidToken(3600);
        
        when(apiClient.login(eq(username), any(String.class)))
            .thenReturn(new AuthResponse(invalidToken, username, "test@example.com"))
            .thenReturn(new AuthResponse(validToken, username, "test@example.com"));

        // First call gets invalid token
        tokenManager.getValidToken(username, password1);

        // Act - Second call should refresh because token is invalid
        String actualToken = tokenManager.getValidToken(username, password2);

        // Assert
        assertEquals(validToken, actualToken);
        verify(apiClient, times(2)).login(eq(username), any(String.class));
    }

    /**
     * Helper method to create a JWT token with a specific expiration time
     * @param expiresInSeconds Number of seconds from now when token expires (negative for past)
     * @return A JWT-like token string
     */
    private String createValidToken(long expiresInSeconds) {
        long expirationTime = Instant.now().getEpochSecond() + expiresInSeconds;
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(
            String.format("{\"sub\":\"testuser\",\"exp\":%d,\"iat\":%d}", expirationTime, Instant.now().getEpochSecond()).getBytes()
        );
        String signature = Base64.getUrlEncoder().encodeToString("fake-signature".getBytes());
        return header + "." + payload + "." + signature;
    }

    /**
     * Helper method to check if a char array has been cleared (filled with zeros)
     */
    private boolean isArrayCleared(char[] array) {
        for (char c : array) {
            if (c != '\0') {
                return false;
            }
        }
        return true;
    }
}
