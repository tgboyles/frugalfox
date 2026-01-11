package com.tgboyles.frugalfoxmcp.config;

import com.tgboyles.frugalfoxmcp.service.CredentialsHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Unit tests for SseCredentialsFilter to verify:
 * - Credentials extracted from headers for SSE endpoints
 * - Non-SSE requests are passed through without processing
 * - Filter chain is always called
 */
@ExtendWith(MockitoExtension.class)
class SseCredentialsFilterTest {

    @Mock
    private CredentialsHolder credentialsHolder;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private ServletResponse response;

    private SseCredentialsFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SseCredentialsFilter(credentialsHolder);
    }

    @Test
    void doFilter_SseEndpointWithCredentials_ExtractsAndStoresCredentials() throws IOException, ServletException {
        // Arrange
        // Filter checks endsWith("/sse") so both "/sse" and "/mcp/sse" would match
        // Using "/mcp/sse" as it's the realistic production endpoint path
        when(httpRequest.getRequestURI()).thenReturn("/mcp/sse");
        when(httpRequest.getHeader("X-Frugalfox-Username")).thenReturn("testuser");
        when(httpRequest.getHeader("X-Frugalfox-Password")).thenReturn("testpass");

        // Act
        filter.doFilter(httpRequest, response, filterChain);

        // Assert
        verify(credentialsHolder).setCredentials("testuser", "testpass");
        verify(filterChain).doFilter(httpRequest, response);
    }

    @Test
    void doFilter_SseEndpointWithoutUsername_DoesNotSetCredentials() throws IOException, ServletException {
        // Arrange
        when(httpRequest.getRequestURI()).thenReturn("/mcp/sse");
        when(httpRequest.getHeader("X-Frugalfox-Username")).thenReturn(null);
        when(httpRequest.getHeader("X-Frugalfox-Password")).thenReturn("testpass");

        // Act
        filter.doFilter(httpRequest, response, filterChain);

        // Assert
        verify(credentialsHolder, never()).setCredentials(any(), any());
        verify(filterChain).doFilter(httpRequest, response);
    }

    @Test
    void doFilter_SseEndpointWithoutPassword_DoesNotSetCredentials() throws IOException, ServletException {
        // Arrange
        when(httpRequest.getRequestURI()).thenReturn("/mcp/sse");
        when(httpRequest.getHeader("X-Frugalfox-Username")).thenReturn("testuser");
        when(httpRequest.getHeader("X-Frugalfox-Password")).thenReturn(null);

        // Act
        filter.doFilter(httpRequest, response, filterChain);

        // Assert
        verify(credentialsHolder, never()).setCredentials(any(), any());
        verify(filterChain).doFilter(httpRequest, response);
    }

    @Test
    void doFilter_NonSseEndpoint_DoesNotProcessCredentials() throws IOException, ServletException {
        // Arrange
        when(httpRequest.getRequestURI()).thenReturn("/api/expenses");

        // Act
        filter.doFilter(httpRequest, response, filterChain);

        // Assert
        verify(credentialsHolder, never()).setCredentials(any(), any());
        verify(filterChain).doFilter(httpRequest, response);
    }

    @Test
    void doFilter_SseEndpointCaseSensitive_DoesNotMatch() throws IOException, ServletException {
        // Arrange
        // Filter uses endsWith("/sse") which is case-sensitive
        // "/mcp/SSE" won't match because it ends with "/SSE" not "/sse"
        when(httpRequest.getRequestURI()).thenReturn("/mcp/SSE");

        // Act
        filter.doFilter(httpRequest, response, filterChain);

        // Assert
        verify(credentialsHolder, never()).setCredentials(any(), any());
        verify(filterChain).doFilter(httpRequest, response);
    }

    @Test
    void doFilter_NonHttpRequest_PassesThrough() throws IOException, ServletException {
        // Arrange
        ServletRequest nonHttpRequest = mock(ServletRequest.class);

        // Act
        filter.doFilter(nonHttpRequest, response, filterChain);

        // Assert
        verify(credentialsHolder, never()).setCredentials(any(), any());
        verify(filterChain).doFilter(nonHttpRequest, response);
    }

    @Test
    void doFilter_SseEndpointWithEmptyCredentials_SetsEmptyCredentials() throws IOException, ServletException {
        // Arrange
        when(httpRequest.getRequestURI()).thenReturn("/mcp/sse");
        when(httpRequest.getHeader("X-Frugalfox-Username")).thenReturn("");
        when(httpRequest.getHeader("X-Frugalfox-Password")).thenReturn("");

        // Act
        filter.doFilter(httpRequest, response, filterChain);

        // Assert
        verify(credentialsHolder).setCredentials("", "");
        verify(filterChain).doFilter(httpRequest, response);
    }
}
