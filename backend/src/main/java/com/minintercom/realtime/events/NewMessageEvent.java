package com.minintercom.realtime.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Event triggered when a new message is sent.
 */
public class NewMessageEvent extends RealtimeEvent {

    @JsonProperty("payload")
    private Payload payload;

    public NewMessageEvent() {
        super("new_message", null, null);
    }

    public NewMessageEvent(UUID tenantId, UUID conversationId, UUID messageId, UUID senderId, String senderType,
            String text) {
        super("new_message", tenantId, conversationId);
        this.payload = new Payload(messageId, senderId, senderType, text);
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public static class Payload {
        @JsonProperty("message_id")
        private UUID messageId;

        @JsonProperty("sender_id")
        private UUID senderId;

        @JsonProperty("sender_type")
        private String senderType;

        @JsonProperty("text")
        private String text;

        public Payload() {
        }

        public Payload(UUID messageId, UUID senderId, String senderType, String text) {
            this.messageId = messageId;
            this.senderId = senderId;
            this.senderType = senderType;
            this.text = text;
        }

        // Getters and Setters
        public UUID getMessageId() {
            return messageId;
        }

        public void setMessageId(UUID messageId) {
            this.messageId = messageId;
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
    }
}
