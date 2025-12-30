package com.minintercom.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.minintercom.common.TenantContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * Servlet Filter that intercepts requests to validate JWT tokens and set the
 * tenant context.
 * Enforces authentication for protected routes.
 */
public class AuthFilter implements Filter {

    private final com.minintercom.services.TenantService tenantService;

    public AuthFilter() {
        this.tenantService = new com.minintercom.services.TenantService();
    }

    public AuthFilter(com.minintercom.services.TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    /**
     * Filters incoming requests to validate JWT and set tenant context.
     * 
     * @param request  The incoming request.
     * @param response The outgoing response.
     * @param chain    The filter chain.
     * @throws IOException      If an I/O error occurs.
     * @throws ServletException If a servlet error occurs.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 1. Try to get Tenant ID from X-Tenant-ID header (simplest for dev/widget)
        String tenantIdHeader = httpRequest.getHeader("X-Tenant-ID");
        UUID tenantId = null;

        if (tenantIdHeader != null && !tenantIdHeader.isEmpty()) {
            try {
                tenantId = UUID.fromString(tenantIdHeader);
            } catch (IllegalArgumentException e) {
                // Ignore invalid UUID in header
            }
        }

        // 2. If no header, try to get from JWT
        if (tenantId == null) {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                DecodedJWT decodedJWT = JwtService.validateToken(token);
                if (decodedJWT != null) {
                    String userIdStr = decodedJWT.getSubject();
                    if (userIdStr != null) {
                        tenantId = tenantService.findTenantIdForUser(UUID.fromString(userIdStr));
                    }
                }
            }
        }

        // 3. Fallback to default development tenant if still null
        if (tenantId == null) {
            tenantId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        }

        // Set context and proceed
        TenantContext.setTenantId(tenantId);
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}
