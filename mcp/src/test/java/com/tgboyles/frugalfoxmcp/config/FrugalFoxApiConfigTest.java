package com.tgboyles.frugalfoxmcp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FrugalFoxApiConfig to verify:
 * - Default configuration values
 * - Getters and setters work correctly
 * - Non-null constraints
 */
class FrugalFoxApiConfigTest {

    private FrugalFoxApiConfig config;

    @BeforeEach
    void setUp() {
        config = new FrugalFoxApiConfig();
    }

    @Test
    void getBaseUrl_DefaultValue_ReturnsLocalhost() {
        // Act
        String baseUrl = config.getBaseUrl();

        // Assert
        assertEquals("http://localhost:8080", baseUrl);
    }

    @Test
    void setBaseUrl_ValidUrl_StoresCorrectly() {
        // Arrange
        String expectedUrl = "http://api.example.com:9090";

        // Act
        config.setBaseUrl(expectedUrl);

        // Assert
        assertEquals(expectedUrl, config.getBaseUrl());
    }

    @Test
    void getTimeout_DefaultValue_Returns30000() {
        // Act
        long timeout = config.getTimeout();

        // Assert
        assertEquals(30000L, timeout);
    }

    @Test
    void setTimeout_ValidValue_StoresCorrectly() {
        // Arrange
        long expectedTimeout = 60000L;

        // Act
        config.setTimeout(expectedTimeout);

        // Assert
        assertEquals(expectedTimeout, config.getTimeout());
    }

    @Test
    void setTimeout_ZeroValue_StoresCorrectly() {
        // Arrange
        long expectedTimeout = 0L;

        // Act
        config.setTimeout(expectedTimeout);

        // Assert
        assertEquals(expectedTimeout, config.getTimeout());
    }

    @Test
    void setTimeout_NegativeValue_StoresValue() {
        // Arrange
        long expectedTimeout = -1000L;

        // Act
        config.setTimeout(expectedTimeout);

        // Assert
        assertEquals(expectedTimeout, config.getTimeout());
    }
}
