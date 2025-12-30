package com.minintercom.security;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet Filter that handles Cross-Origin Resource Sharing (CORS) headers.
 * Allows the frontend application to communicate with the backend API.
 */
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Allow requests from the frontend origin (adjust as needed for production)
        // For development, allowing all or specific localhost is common.
        // Ideally, this should be configurable via environment variables.
        httpResponse.setHeader("Access-Control-Allow-Origin", "*"); // OR "http://localhost:3000"
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization, X-Requested-With, X-Tenant-ID");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // Handle preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}
