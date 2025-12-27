package com.minintercom.realtime.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Event triggered when an agent starts or stops typing.
 */
public class AgentTypingEvent extends RealtimeEvent {

    @JsonProperty("payload")
    private Payload payload;

    public AgentTypingEvent() {
        super("agent_typing", null, null);
    }

    public AgentTypingEvent(UUID tenantId, UUID conversationId, UUID agentId, boolean isTyping) {
        super("agent_typing", tenantId, conversationId);
        this.payload = new Payload(agentId, isTyping);
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public static class Payload {
        @JsonProperty("agent_id")
        private UUID agentId;

        @JsonProperty("is_typing")
        private boolean isTyping;

        public Payload() {
        }

        public Payload(UUID agentId, boolean isTyping) {
            this.agentId = agentId;
            this.isTyping = isTyping;
        }

        // Getters and Setters
        public UUID getAgentId() {
            return agentId;
        }

        public void setAgentId(UUID agentId) {
            this.agentId = agentId;
        }

        public boolean isTyping() {
            return isTyping;
        }

        public void setTyping(boolean typing) {
            isTyping = typing;
        }
    }
}
