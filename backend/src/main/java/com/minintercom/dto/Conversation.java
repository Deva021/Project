package com.minintercom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;
import java.sql.Timestamp;

/**
 * Data Transfer Object for a chat conversation.
 */
public class Conversation {
    private UUID id;
    @JsonProperty("tenant_id")
    private UUID tenantId;
    private String title;
    private Timestamp createdAt;

    public Conversation() {
    }

    public Conversation(UUID id, UUID tenantId, String title) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
    }

    public Conversation(UUID id, UUID tenantId, String title, Timestamp createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
