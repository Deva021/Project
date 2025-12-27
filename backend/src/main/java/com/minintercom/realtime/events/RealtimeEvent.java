package com.minintercom.realtime.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;

/**
 * Base class for all real-time events.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NewMessageEvent.class, name = "new_message"),
        @JsonSubTypes.Type(value = AgentTypingEvent.class, name = "agent_typing"),
        @JsonSubTypes.Type(value = ConversationStatusUpdateEvent.class, name = "conversation_status_update")
})
public abstract class RealtimeEvent {

    @JsonProperty("type")
    private String type;

    @JsonProperty("tenant_id")
    private UUID tenantId;

    @JsonProperty("conversation_id")
    private UUID conversationId;

    @JsonProperty("timestamp")
    private long timestamp;

    public RealtimeEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public RealtimeEvent(String type, UUID tenantId, UUID conversationId) {
        this();
        this.type = type;
        this.tenantId = tenantId;
        this.conversationId = conversationId;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
