package com.minintercom.realtime.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Event triggered when a conversation status is updated.
 */
public class ConversationStatusUpdateEvent extends RealtimeEvent {

    @JsonProperty("payload")
    private Payload payload;

    public ConversationStatusUpdateEvent() {
        super("conversation_status_update", null, null);
    }

    public ConversationStatusUpdateEvent(UUID tenantId, UUID conversationId, String newStatus) {
        super("conversation_status_update", tenantId, conversationId);
        this.payload = new Payload(newStatus);
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public static class Payload {
        @JsonProperty("new_status")
        private String newStatus;

        public Payload() {
        }

        public Payload(String newStatus) {
            this.newStatus = newStatus;
        }

        // Getters and Setters
        public String getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(String newStatus) {
            this.newStatus = newStatus;
        }
    }
}
