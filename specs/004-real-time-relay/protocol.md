# Real-Time Relay Protocol Specification

This document defines the custom TCP-based protocol used for communication between the backend services, the Relay Server, and connected clients.

## 1. Overview

The protocol is designed to be lightweight and efficient, using a simple line-based JSON format over persistent TCP connections.

## 2. Message Format

Each message is a single JSON object followed by a newline character (`\n`).

### 2.1. Common Fields

| Field       | Type   | Description                                                   |
| :---------- | :----- | :------------------------------------------------------------ |
| `type`      | String | The type of message (e.g., `CONNECT`, `EVENT`, `DISCONNECT`). |
| `timestamp` | Long   | Unix timestamp in milliseconds.                               |

## 3. Message Types

Sent by a client to establish a session.

**Standard Client (Agent/Visitor):**

```json
{
  "type": "CONNECT",
  "tenant_id": "uuid",
  "auth_token": "jwt_token",
  "subscriptions": ["conversation_id_1", "conversation_id_2"]
}
```

**System Client (Internal Backend):**
The `system_key` MUST match the `RELAY_SYSTEM_KEY` environment variable set on the server.

```json
{
  "type": "CONNECT",
  "system_key": "RELAY_SYSTEM_KEY_VALUE"
}
```

### 3.2. `EVENT`

Used to publish or receive real-time events.

```json
{
  "type": "EVENT",
  "event_type": "new_message | agent_typing | conversation_status_update",
  "tenant_id": "uuid",
  "conversation_id": "uuid",
  "payload": { ... }
}
```

#### Event Payloads

**`new_message`**

```json
{
  "message_id": "uuid",
  "sender_id": "uuid",
  "sender_type": "visitor | agent",
  "text": "..."
}
```

**`agent_typing`**

```json
{
  "agent_id": "uuid",
  "is_typing": true
}
```

**`conversation_status_update`**

```json
{
  "new_status": "OPEN | ACTIVE | PENDING | CLOSED"
}
```

### 3.3. `DISCONNECT`

Sent by a client to gracefully close the connection.

```json
{
  "type": "DISCONNECT"
}
```

## 4. Error Handling

If an invalid message or unauthorized request is received, the server will send an `ERROR` message and may close the connection.

```json
{
  "type": "ERROR",
  "code": "UNAUTHORIZED | INVALID_FORMAT",
  "message": "..."
}
```

## 5. Security

- All connections MUST provide a valid `auth_token` (JWT) in the `CONNECT` message.
- The Relay Server will verify the JWT and ensure the user has access to the requested `tenant_id`.
- Event routing will strictly enforce tenant isolation.
