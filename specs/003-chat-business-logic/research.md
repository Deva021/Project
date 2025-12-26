# Research for Chat Business Logic

## 1. Conversation Status Lifecycle

- **Decision**: The conversation status will be an `enum` with the following states: `OPEN`, `ACTIVE`, `PENDING`, `CLOSED`.
  - **`OPEN`**: A new conversation started by a visitor, not yet picked up by an agent.
  - **`ACTIVE`**: An agent has engaged with the conversation.
  - **`PENDING`**: The agent is awaiting a reply from the visitor.
  - **`CLOSED`**: The conversation is resolved and no more messages can be sent.
- **Rationale**: This lifecycle model is simple, comprehensive, and common in customer support applications. It covers the key stages of a conversation from initiation to resolution, allowing agents to effectively manage their queue of active and pending chats. This resolves the `[NEEDS CLARIFICATION]` item from the implementation plan.
- **Alternatives considered**: A simpler `open`/`closed` model was considered but deemed insufficient for an agent to manage their workload effectively. More complex models including states like `wrap-up` were considered over-engineering for the initial implementation.
