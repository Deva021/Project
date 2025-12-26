package com.minintercom.dto;

import java.util.UUID;
import java.sql.Timestamp;

/**
 * Data Transfer Object for a chat conversation.
 */
public class Conversation {
    private UUID id;
    private UUID tenantId;
    private String status;
    private Timestamp createdAt;

    public Conversation() {
    }

    public Conversation(UUID id, UUID tenantId, String status, Timestamp createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
