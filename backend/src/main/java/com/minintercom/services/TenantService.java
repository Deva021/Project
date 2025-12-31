package com.minintercom.services;

import com.minintercom.common.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Service for managing tenant-related operations.
 */
public class TenantService {

    /**
     * Finds the tenant ID associated with a given user ID.
     * Assumes a user belongs to at least one tenant. Returns the first one found.
     *
     * @param userId The UUID of the user.
     * @return The UUID of the tenant, or null if not found.
     */
    public UUID findTenantIdForUser(UUID userId) {
        String sql = "SELECT tenant_id FROM tenant_memberships WHERE user_id = ? LIMIT 1";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject("tenant_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
