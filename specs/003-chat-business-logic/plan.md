# Chat Business Logic - Implementation Plan

## 1. Objective

To implement the backend business logic for managing chat conversations and messages. This includes creating services and API endpoints for creating conversations, sending messages, and listing conversations/messages, all while enforcing strict tenant isolation.

## 2. Technical Context
- **Backend Stack**: Java, Servlets.
- **Dependencies**: This feature builds upon the `002-backend-core-services` feature. It will use the existing `DatabaseService`, `JwtService`, and `AuthFilter`.
- **Key Components**:
  1.  `ConversationService`: A service class containing logic for `createConversation`, `listConversations`, `updateConversationStatus`.
  2.  `MessageService`: A service class containing logic for `sendMessage` and `getMessageHistory`.
  3.  `ChatServlet`: A new `HttpServlet` to handle all API routes related to chat (e.g., `/api/conversations`, `/api/conversations/{id}/messages`).
  4.  `Conversation` and `Message` Data Transfer Objects (DTOs).
- **Clarifications**: All clarifications have been resolved through research.
- **Conversation Status Lifecycle (from research.md)**:
    - **`OPEN`**: A new conversation started by a visitor, not yet picked up by an agent.
    - **`ACTIVE`**: An agent has engaged with the conversation.
    - **`PENDING`**: The agent is awaiting a reply from the visitor.
    - **`CLOSED`**: The conversation is resolved and no more messages can be sent.

## 3. Constitution Checklist

- [ ] **Principle 1: Server Components by Default:** [N/A] Backend-only feature.
- [x] **Principle 2: Strict Tenant Isolation:** The new services will correctly use the `TenantContext` for all database queries via the `DatabaseService`. RLS policies will be added to the new `conversations` and `messages` tables.
- [x] **Principle 3: Robust Concurrency:** The new services will be designed to be stateless and thread-safe, aligning with established core service patterns.
- [x] **Principle 4: High-Performance UX:** The plan incorporates pagination for listing conversations and messages to meet performance budgets, as defined in `spec.md`.
- [x] **Principle 5: Specification-First:** This plan is based on the approved specification `003-chat-business-logic/spec.md`.

## 4. Implementation Steps

| Step | Description | Owner | Status |
| :--- | :---------- | :---- | :----- |
| 1.   | Phase 0: Research and define the conversation status lifecycle. | TBD | Done   |
| 2.   | Create Supabase migration for `conversations` and `messages` tables. | TBD | To Do  |
| 3.   | Implement `Conversation` and `Message` model classes. | TBD | To Do  |
| 4.   | Implement `ConversationService`. | TBD | To Do  |
| 5.   | Implement `MessageService`. | TBD | To Do  |
| 6.   | Implement `ChatServlet` to handle API requests. | TBD | To Do  |
| 7.   | Write unit and integration tests for new services and endpoints. | TBD | To Do  |

## 5. Testing Strategy

- **Unit Tests**: The logic within `ConversationService` and `MessageService` will be unit-tested using mocks for the `DatabaseService`.
- **Integration Tests**: A suite of integration tests will target the `ChatServlet`. These tests will use an embedded server (Jetty) and a test database (Testcontainers) to simulate API calls for creating conversations, sending messages, and listing data, verifying tenant isolation rules are enforced.