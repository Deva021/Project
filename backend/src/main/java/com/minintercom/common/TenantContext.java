package com.minintercom.common;

import java.util.UUID;

/**
 * ThreadLocal storage for the current tenant ID.
 * This ensures that the tenant context is available throughout the request processing lifecycle
 * and can be used by the DatabaseService to enforce tenant isolation.
 */
public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    /**
     * Sets the tenant ID for the current thread.
     * @param tenantId The UUID of the tenant.
     */
    public static void setTenantId(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Retrieves the tenant ID for the current thread.
     * @return The UUID of the current tenant, or null if not set.
     */
    public static UUID getTenantId() {
        return currentTenant.get();
    }

    /**
     * Clears the tenant ID from the current thread.
     * Should be called in a finally block to prevent context leakage.
     */
    public static void clear() {
        currentTenant.remove();
    }
}
