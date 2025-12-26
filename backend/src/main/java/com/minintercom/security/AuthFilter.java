package com.minintercom.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.minintercom.common.TenantContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Servlet Filter that intercepts requests to validate JWT tokens and set the tenant context.
 * Enforces authentication for protected routes.
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    /**
     * Filters incoming requests to validate JWT and set tenant context.
     * @param request The incoming request.
     * @param response The outgoing response.
     * @param chain The filter chain.
     * @throws IOException If an I/O error occurs.
     * @throws ServletException If a servlet error occurs.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        DecodedJWT decodedJWT = JwtService.validateToken(token);

        if (decodedJWT == null) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        try {
            String tenantIdStr = JwtService.getClaim(decodedJWT, "tenant_id");
            if (tenantIdStr != null) {
                TenantContext.setTenantId(UUID.fromString(tenantIdStr));
            }
            
            // Proceed with the request
            chain.doFilter(request, response);
            
        } catch (IllegalArgumentException e) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID format in token");
        } finally {
            // CRITICAL: Clear the context to prevent leakage between threads
            TenantContext.clear();
        }
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}
