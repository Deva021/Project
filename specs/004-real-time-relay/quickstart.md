# Quickstart: Real-Time Layer

This guide provides examples for interacting with the Real-Time Layer, focusing on the long-polling API for event retrieval.

## 1. Poll for Real-Time Events

This endpoint allows clients to long-poll for events. The connection will remain open until an event is available or the specified `timeoutSeconds` are reached.

### Request

```bash
curl -X GET "http://localhost:8080/api/realtime/poll?tenantId={YOUR_TENANT_UUID}&conversationId={OPTIONAL_CONVERSATION_UUID}&timeoutSeconds=30" \
-H "Accept: application/json"
```

Replace `{YOUR_TENANT_UUID}` with a valid tenant ID.
Optionally replace `{OPTIONAL_CONVERSATION_UUID}` with a specific conversation ID to filter events.

### Response (Example - New Message Event)

```json
[
  {
    "type": "new_message",
    "timestamp": 1703673600,
    "tenantId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
    "conversationId": "c1c2c3c4-d5d6-7890-1234-567890abcdef",
    "messageId": "m1m2m3m4-n5n6-7890-1234-567890abcdef",
    "senderId": null,
    "senderType": "VISITOR",
    "text": "Hello, I need assistance.",
    "isTyping": false
  }
]
```

### Response (Example - No Events / Timeout)

If no events are available within the timeout period, a 204 No Content response will be returned.

### Note on Custom Real-Time Protocol

Interaction with the core Relay Server (e.g., publishing events from the backend via the Relay Client, or direct client connections using the custom TCP protocol) will follow the specifications outlined in the `Relay Server Protocol Specification` document. This `quickstart.md` primarily focuses on the HTTP-based long-polling mechanism.
