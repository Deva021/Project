# Quickstart: Using the Backend Core Services

This guide provides examples of how to use the new core services in the backend.

## 1. Tenant-Aware Data Access

The `AuthFilter` automatically handles tenant identification. In your own services, you can rely on the `DatabaseService` to correctly scope queries.

### Example: Fetching data for the current tenant

```java
// In your application service
public class UserProfileService {

    private final DatabaseService dbService;

    // Assumes DatabaseService is injected
    public UserProfileService(DatabaseService dbService) {
        this.dbService = dbService;
    }

    public User getCurrentUserProfile() {
        // The tenant ID is automatically retrieved from TenantContext
        // within the DatabaseService implementation.
        UUID currentUserId = ... // Get user ID from security context
        return dbService.findUserById(currentUserId);
    }
}
```

## 2. JWT Validation in the `AuthFilter`

The `AuthFilter` is the primary consumer of the `JwtService`. You typically won't need to call it directly.

### `AuthFilter` Logic Pseudocode

```java
public class AuthFilter implements Filter {

    private JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String token = extractTokenFromHeader(request);
        try {
            DecodedJWT decodedJWT = jwtService.validateToken(token);
            UUID tenantId = jwtService.getTenantId(decodedJWT);

            // Set tenant for the rest of the request lifecycle
            TenantContext.setCurrentTenant(tenantId);

            // Add user identity to request attribute for downstream services
            request.setAttribute("userPrincipal", ...);

            chain.doFilter(request, response);

        } catch (JWTVerificationException e) {
            // Token is invalid, reject the request
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        } finally {
            // CRITICAL: Always clear the context
            TenantContext.clear();
        }
    }
}
```
