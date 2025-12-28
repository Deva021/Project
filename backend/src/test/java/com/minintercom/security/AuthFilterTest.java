package com.minintercom.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.minintercom.common.TenantContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthFilterTest {

    private AuthFilter authFilter;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockChain;

    private static String testSecret;
    private static Algorithm testAlgorithm;

    @BeforeAll
    static void initSecret() {
        try (InputStream input = AuthFilterTest.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                testSecret = prop.getProperty("jwt.secret");
            }
            if (testSecret == null || testSecret.isEmpty()) {
                testSecret = "test-secret-key-for-unit-tests-only"; // Fallback, though it should be in properties
            }
        } catch (IOException e) {
            e.printStackTrace();
            testSecret = "test-secret-key-for-unit-tests-only"; // Fallback in case of error
        }
        testAlgorithm = Algorithm.HMAC256(testSecret);
    }

    @BeforeEach
    public void setUp() {
        authFilter = new AuthFilter();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockChain = mock(FilterChain.class);
        TenantContext.clear();
    }

    @Test
    public void testFilterWithValidToken() throws IOException, ServletException {
        UUID tenantId = UUID.randomUUID();
        String token = JWT.create()
                .withClaim("tenant_id", tenantId.toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000)) // Token valid for 1 hour
                .sign(testAlgorithm); // Use the correct algorithm initialized with testSecret

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + token);

        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        verify(mockChain).doFilter(mockRequest, mockResponse);
        assertNull(TenantContext.getTenantId()); // Should be cleared in finally block
    }

    @Test
    public void testFilterWithInvalidToken() throws IOException, ServletException {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        verify(mockResponse).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verify(mockChain, never()).doFilter(any(), any());
    }

    @Test
    public void testFilterWithMissingHeader() throws IOException, ServletException {
        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        authFilter.doFilter(mockRequest, mockResponse, mockChain);

        verify(mockResponse).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verify(mockChain, never()).doFilter(any(), any());
    }
}
