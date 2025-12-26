package com.minintercom.dto;

import java.util.UUID;
import java.sql.Timestamp;

/**
 * Data Transfer Object for a chat message.
 */
public class Message {
    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private String senderType;
    private String text;
    private Timestamp createdAt;

    public Message() {
    }

    public Message(UUID id, UUID conversationId, UUID senderId, String senderType, String text, Timestamp createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.text = text;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
