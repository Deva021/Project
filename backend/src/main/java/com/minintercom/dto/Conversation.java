package com.minintercom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;
import java.sql.Timestamp;

/**
 * Data Transfer Object for a chat conversation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Conversation {
    private UUID id;
    @JsonProperty("tenant_id")
    private UUID tenantId;
    private String title;
    private String status;
    private Timestamp createdAt;

    public Conversation() {
    }

    public Conversation(UUID id, UUID tenantId, String title, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
        this.status = status;
    }

    public Conversation(UUID id, UUID tenantId, String title, String status, Timestamp createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
