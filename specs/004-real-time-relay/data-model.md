# Data Model for Real-Time Relay

This feature primarily focuses on establishing a real-time communication infrastructure and does not introduce new persistent data entities in the database. Instead, it leverages and interacts with existing data models (e.g., `conversations`, `messages`, `tenants`) from previous features.

The "data" involved in this feature are primarily the real-time **events** and their associated **payloads**. These events are transient and are processed and routed, not persisted by this layer.

## Event Data Structures

The following conceptual data structures represent the payloads of events that will be transmitted through the real-time layer. These are logical structures, not database tables.

### `NewMessageEvent`

Represents a new message being sent in a conversation.

| Field           | Type    | Description                                             |
|-----------------|---------|---------------------------------------------------------|
| `type`          | `string`| Always "new_message"                                    |
| `conversationId`| `string`| UUID of the conversation the message belongs to.        |
| `tenantId`      | `string`| UUID of the tenant the conversation belongs to.         |
| `messageId`     | `string`| UUID of the new message.                                |
| `senderId`      | `string`| (Optional) UUID of the sender (agent). Null for visitor.|
| `senderType`    | `string`| "VISITOR" or "AGENT"                                    |
| `text`          | `string`| Content of the message.                                 |
| `timestamp`     | `long`  | Unix timestamp when the message was created.            |

### `AgentTypingEvent`

Represents an agent typing activity in a conversation. This is a transient event.

| Field           | Type    | Description                                             |
|-----------------|---------|---------------------------------------------------------|
| `type`          | `string`| Always "agent_typing"                                   |
| `conversationId`| `string`| UUID of the conversation where typing is occurring.     |
| `tenantId`      | `string`| UUID of the tenant.                                     |
| `agentId`       | `string`| UUID of the agent who is typing.                        |
| `isTyping`      | `boolean`| True if typing, false if stopped.                      |

### `ConversationStatusUpdateEvent`

Represents a change in the status of a conversation.

| Field           | Type    | Description                                             |
|-----------------|---------|---------|
| `type`          | `string`| Always "conversation_status_update"                     |
| `conversationId`| `string`| UUID of the conversation being updated.                 |
| `tenantId`      | `string`| UUID of the tenant.                                     |
| `newStatus`     | `string`| The new status of the conversation (e.g., "ACTIVE", "CLOSED"). |
| `timestamp`     | `long`  | Unix timestamp of the status change.                    |
