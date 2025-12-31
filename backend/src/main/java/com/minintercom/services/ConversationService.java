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
     * Lists conversations for a specific tenant, optionally filtered by status.
     *
     * @param tenantId The UUID of the tenant.
     * @param status   The status to filter by (optional).
     * @return A list of conversations.
     */
    public List<Conversation> getConversationsByTenantId(UUID tenantId, String status) throws SQLException {
        List<Conversation> conversations = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, tenant_id, title, status, created_at FROM conversations WHERE tenant_id = ?");
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
        }

        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setObject(1, tenantId);
            if (status != null && !status.isEmpty()) {
                pstmt.setString(2, status);
            }

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
     * Gets a single conversation by ID.
     *
     * @param conversationId The UUID of the conversation.
     * @return The Conversation object or null if not found.
     */
    public Conversation getConversationById(UUID conversationId) throws SQLException {
        String sql = "SELECT id, tenant_id, title, status, created_at FROM conversations WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, conversationId);

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
     * Updates the status of a conversation.
     *
     * @param conversationId The UUID of the conversation.
     * @param status         The new status.
     */
    public void updateConversationStatus(UUID conversationId, String status) throws SQLException {
        String sql = "UPDATE conversations SET status = ? WHERE id = ? RETURNING tenant_id";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    /**
     * Deletes closed conversations older than 24 hours.
     */
    public void cleanupOldClosedConversations() throws SQLException {
        String sql = "SELECT delete_old_closed_conversations()";
        try (Connection conn = DatabaseService.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
