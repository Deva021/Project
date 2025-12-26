# Data Model for Chat Business Logic

This feature introduces two new tables to the database. RLS policies will be added to both tables to enforce tenant isolation.

## `conversations` Table

Stores information about a single chat conversation.

| Column      | Type      | Constraints                               | Description                                         |
|-------------|-----------|-------------------------------------------|-----------------------------------------------------|
| `id`        | `uuid`    | Primary Key, default `gen_random_uuid()`  | Unique identifier for the conversation.             |
| `tenant_id` | `uuid`    | Not Null, Foreign Key to `tenants.id`     | The tenant this conversation belongs to.            |
| `status`    | `varchar` | Not Null, Check in ('OPEN', 'ACTIVE', 'PENDING', 'CLOSED') | The current status of the conversation.             |
| `created_at`| `timestamptz` | Not Null, default `now()`               | Timestamp of when the conversation was created.     |
| `updated_at`| `timestamptz` | Not Null, default `now()`               | Timestamp of the last update.                       |

## `messages` Table

Stores an individual message within a conversation.

| Column          | Type      | Constraints                               | Description                                         |
|-----------------|-----------|-------------------------------------------|-----------------------------------------------------|
| `id`            | `uuid`    | Primary Key, default `gen_random_uuid()`  | Unique identifier for the message.                  |
| `conversation_id`| `uuid`   | Not Null, Foreign Key to `conversations.id`| The conversation this message belongs to.           |
| `sender_id`     | `uuid`    | Nullable, Foreign Key to `users.id`       | The authenticated user (agent) who sent the message. Null for visitors. |
| `sender_type`   | `varchar` | Not Null, Check in ('VISITOR', 'AGENT')   | The type of sender.                                 |
| `text`          | `text`    | Not Null                                  | The content of the message.                         |
| `created_at`    | `timestamptz`| Not Null, default `now()`               | Timestamp of when the message was sent.             |
