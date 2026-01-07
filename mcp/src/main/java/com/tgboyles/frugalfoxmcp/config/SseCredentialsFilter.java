package com.tgboyles.frugalfoxmcp.config;

import com.tgboyles.frugalfoxmcp.service.CredentialsHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that extracts username and password from HTTP headers.
 * This allows the MCP server to automatically manage JWT tokens without requiring
 * the client to pass tokens on every request.
 *
 * Expected headers:
 * - X-Frugalfox-Username: The username for authentication
 * - X-Frugalfox-Password: The password for authentication
 */
@Component
@Order(1)
public class SseCredentialsFilter implements Filter {
    
    private static final String USERNAME_HEADER = "X-Frugalfox-Username";
    private static final String PASSWORD_HEADER = "X-Frugalfox-Password";

    private final CredentialsHolder credentialsHolder;

    public SseCredentialsFilter(CredentialsHolder credentialsHolder) {
        this.credentialsHolder = credentialsHolder;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            String uri = httpRequest.getRequestURI();

            // Only process SSE endpoint requests
            if (uri.endsWith("/sse")) {
                String username = httpRequest.getHeader(USERNAME_HEADER);
                String password = httpRequest.getHeader(PASSWORD_HEADER);

                if (username != null && password != null) {
                    credentialsHolder.setCredentials(username, password);
                } 
            }
        }

        chain.doFilter(request, response);
    }
}
