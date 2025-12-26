package com.minintercom.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.minintercom.common.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthFilterTest {

    private AuthFilter authFilter;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockChain;

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
        String secret = "your-default-secret-key-change-this-in-production";
        String token = JWT.create()
                .withClaim("tenant_id", tenantId.toString())
                .sign(Algorithm.HMAC256(secret));

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
