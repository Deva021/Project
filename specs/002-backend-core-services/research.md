# Research for Backend Core Services

## 1. Java JWT Library Selection

- **Decision**: `com.auth0:java-jwt`
- **Rationale**: The feature requires robust JWT signing and verification (JWS). The `auth0:java-jwt` library is lightweight, focuses specifically on JWS, is backed by a reputable company in the identity space (Auth0), and has a straightforward API. It meets all feature requirements without introducing unnecessary complexity (like JWE features found in more comprehensive libraries).
- **Alternatives considered**: `io.jsonwebtoken:jjwt`. While powerful and more feature-rich (implementing the full JOSE spec), its added complexity is not required for this feature.

## 2. Tenant Isolation Strategy

- **Decision**: A combination of a `ServletFilter` and a `ThreadLocal`-based context holder will be used to enforce tenant isolation in the shared-schema database.
- **Pattern**:
    1.  The `AuthFilter` will extract the `tenant_id` from the validated JWT claims.
    2.  The `tenant_id` will be stored in a static `ThreadLocal` variable within a `TenantContext` class (e.g., `TenantContext.setCurrentTenant(tenantId)`).
    3.  The `DatabaseService` will read the `tenant_id` from `TenantContext.getCurrentTenant()` and automatically apply it as a `WHERE` clause to all relevant queries it executes. This ensures that data access is always scoped to the current tenant.
    4.  A `finally` block within the `AuthFilter` will guarantee that `TenantContext.clear()` is called after each request to prevent tenant ID leakage between threads.
- **Rationale**: This pattern provides a robust, centralized, and low-overhead mechanism for enforcing tenant isolation, directly addressing the violation of Principle 2 (Strict Tenant Isolation). It avoids error-prone manual passing of `tenant_id` through every method call and is a standard industry practice for multi-tenant applications with a shared database. This will be supplemented by Supabase Row-Level Security (RLS) policies as a secondary enforcement layer.
- **Alternatives considered**:
    - **Database-per-tenant / Schema-per-tenant**: Not viable with the current single-instance Supabase architecture.
    - **Manual `tenant_id` passing**: Considered too brittle and prone to developer error. The `ThreadLocal` context approach is safer and more maintainable.
