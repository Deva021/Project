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
     * Creates a new conversation for a tenant, optionally with a title.
     *
     * @param conversation The Conversation object containing details like title.
     * @param visitorName  The name of the visitor initiating the conversation (can
     *                     be null).
     * @return The created Conversation object.
     */
    public Conversation createConversation(Conversation conversation, String visitorName) throws SQLException {
        String sql = "INSERT INTO conversations (tenant_id, title, status) VALUES (?, ?, 'OPEN') RETURNING id, tenant_id, title, status, created_at";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, conversation.getTenantId());
            pstmt.setString(2, conversation.getTitle()); // Assuming title is set in the conversation object

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Conversation(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("tenant_id"),
                            rs.getString("title"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at"));
                }
            }
        }
        return null;
    }

    /**
     * Lists all conversations for a specific tenant.
     *
     * @param tenantId The UUID of the tenant whose conversations are to be listed.
     * @return A list of conversations for the given tenant.
     */
    public List<Conversation> getConversationsByTenantId(UUID tenantId) throws SQLException {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT id, tenant_id, title, status, created_at FROM conversations WHERE tenant_id = ?";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, tenantId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    conversations.add(new Conversation(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("tenant_id"),
                            rs.getString("title"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at")));
                }
            }
        }
        return conversations;
    }

    /**
     * Updates the status of a conversation.
     *
     * @param conversationId The UUID of the conversation.
     * @param status         The new status.
     */
    public void updateConversationStatus(UUID conversationId, String status) throws SQLException {
        String sql = "UPDATE conversations SET status = ? WHERE id = ? RETURNING tenant_id";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = DatabaseService.prepareTenantStatement(conn, sql)) {

            pstmt.setString(1, status);
            pstmt.setObject(2, conversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UUID tenantId = (UUID) rs.getObject("tenant_id");
                    com.minintercom.realtime.events.ConversationStatusUpdateEvent event = new com.minintercom.realtime.events.ConversationStatusUpdateEvent(
                            tenantId,
                            conversationId,
                            status);
                    com.minintercom.realtime.client.RealtimeClient.getInstance().publish(event);
                }
            }
        }
    }
}
