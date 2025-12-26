package com.minintercom.services;

import com.minintercom.dto.Conversation;
import com.minintercom.common.DatabaseService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing chat conversations.
 * Provides methods for creating, listing, and updating conversation status.
 */
public class ConversationService {

    /**
     * Creates a new conversation for a tenant.
     * 
     * @param tenantId The UUID of the tenant.
     * @return The created Conversation object.
     */
    public Conversation createConversation(UUID tenantId) {
        String sql = "INSERT INTO conversations (tenant_id, status) VALUES (?, 'OPEN') RETURNING id, tenant_id, status, created_at";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, tenantId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Conversation(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("tenant_id"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lists all conversations for the current tenant.
     * 
     * @return A list of conversations.
     */
    public List<Conversation> listConversations() {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT id, tenant_id, status, created_at FROM conversations";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = DatabaseService.prepareTenantStatement(conn, sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                conversations.add(new Conversation(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("tenant_id"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    /**
     * Updates the status of a conversation.
     * 
     * @param conversationId The UUID of the conversation.
     * @param status         The new status.
     */
    public void updateConversationStatus(UUID conversationId, String status) {
        String sql = "UPDATE conversations SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = DatabaseService.prepareTenantStatement(conn, sql)) {

            pstmt.setString(1, status);
            pstmt.setObject(2, conversationId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
