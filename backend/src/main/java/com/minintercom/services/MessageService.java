package com.minintercom.services;

import com.minintercom.dto.Message;
import com.minintercom.common.DatabaseService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing chat messages.
 * Provides methods for sending messages and retrieving message history.
 */
public class MessageService {

    /**
     * Sends a message in a conversation.
     * 
     * @param conversationId The UUID of the conversation.
     * @param senderId       The UUID of the sender (null for visitors).
     * @param senderType     The type of sender ('visitor' or 'agent').
     * @param text           The message content.
     * @return The created Message object.
     */
    public Message sendMessage(UUID conversationId, UUID senderId, String senderType, String text) throws SQLException {
        String checkStatusSql = "SELECT status FROM conversations WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkStatusSql)) {
            checkStmt.setObject(1, conversationId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if (!"OPEN".equalsIgnoreCase(status)) {
                        throw new SQLException("Cannot send message to a " + status + " conversation.");
                    }
                } else {
                    throw new SQLException("Conversation not found.");
                }
            }
        }

        String sql = "INSERT INTO messages (conversation_id, sender_id, sender_type, text) VALUES (?, ?, ?, ?) RETURNING id, conversation_id, sender_id, sender_type, text, created_at";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, conversationId);
            pstmt.setObject(2, senderId);
            pstmt.setString(3, senderType);
            pstmt.setString(4, text);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Message message = new Message(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("conversation_id"),
                            (UUID) rs.getObject("sender_id"),
                            rs.getString("sender_type"),
                            rs.getString("text"),
                            rs.getTimestamp("created_at"));

                    // Publish real-time event
                    publishNewMessageEvent(message);

                    return message;
                }
            }
        }
        return null;
    }

    private void publishNewMessageEvent(Message message) throws SQLException {
        String sql = "SELECT tenant_id FROM conversations WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, message.getConversationId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UUID tenantId = (UUID) rs.getObject("tenant_id");
                    com.minintercom.realtime.events.NewMessageEvent event = new com.minintercom.realtime.events.NewMessageEvent(
                            tenantId,
                            message.getConversationId(),
                            message.getId(),
                            message.getSenderId(),
                            message.getSenderType(),
                            message.getText());
                    com.minintercom.realtime.client.RealtimeClient.getInstance().publish(event);
                }
            }
        }
    }

    /**
     * Retrieves the message history for a conversation.
     * 
     * @param conversationId The UUID of the conversation.
     * @return A list of messages.
     */
    public List<Message> getMessageHistory(UUID conversationId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, conversation_id, sender_id, sender_type, text, created_at FROM messages WHERE conversation_id = ? ORDER BY created_at ASC";
        try (Connection conn = DatabaseService.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, conversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                            (UUID) rs.getObject("id"),
                            (UUID) rs.getObject("conversation_id"),
                            (UUID) rs.getObject("sender_id"),
                            rs.getString("sender_type"),
                            rs.getString("text"),
                            rs.getTimestamp("created_at")));
                }
            }
        }
        return messages;
    }
}
