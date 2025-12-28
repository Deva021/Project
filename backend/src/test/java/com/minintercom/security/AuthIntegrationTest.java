package com.minintercom.security;

import com.minintercom.servlets.ConversationsServlet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static org.mockito.Mockito.*;

public class AuthIntegrationTest {

    private AuthFilter authFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authFilter = new AuthFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    public void testProtectedEndpointWithoutToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        authFilter.doFilter(request, response, chain);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void testProtectedEndpointWithInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        authFilter.doFilter(request, response, chain);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void testProtectedEndpointWithValidToken() throws Exception {
        // This test requires a valid token from JwtService
        // For a full integration test, we'd need to mock JwtService or create a real one.
        // For now, let's create a valid token mock (this is technically a unit test of the filter)
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0ZW5hbnRfaWQiOiJhYjEyMzQ1Ni03ODkwLTExMjItMzMzMy01Njc4OTA1ZWRlZjAiLCJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.N_dC42_K64c1C-4M2J-0_0_J_3_0_0_0_0_0_0_0"; // Dummy token
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Mock JwtService static method
        try (var mockedStatic = mockStatic(JwtService.class)) {
            mockedStatic.when(() -> JwtService.validateToken(anyString())).thenReturn(mock(com.auth0.jwt.interfaces.DecodedJWT.class));
            mockedStatic.when(() -> JwtService.getClaim(any(), eq("tenant_id"))).thenReturn("ab123456-7890-1122-3333-5678905edef0");

            authFilter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            // Verify TenantContext was set (this requires inspecting TenantContext directly if not exposed)
        }
    }

}
