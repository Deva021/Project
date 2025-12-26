# Feature Specification: Chat Business Logic

## 1. Problem Statement

The application currently lacks the core business logic to manage chat conversations and messages. We need a way for visitors to initiate chats, for agents to respond, and for the system to maintain a history of these interactions in a secure, tenant-aware manner.

## 2. Proposed Solution

We will introduce two new services: a `ConversationService` and a `MessageService`. The `ConversationService` will handle the lifecycle of a conversation (creation, status changes, listing). The `MessageService` will manage the sending and retrieval of individual messages within a conversation. These services will distinguish between public actions (visitor-initiated) and protected, tenant-scoped actions (agent-initiated).

## 3. Technical Design

### 3.1. Frontend (Next.js)
- **Components:** N/A. This is a backend-only feature.
- **State Management:** N/A.
- **Data Fetching:** N/A.

### 3.2. Backend (Java Servlets / TCP Relay)
- **API Endpoints:** This feature will expose new API endpoints for chat functionality.
    - `POST /api/conversations` (Public): Creates a new conversation.
    - `GET /api/conversations` (Protected): Lists conversations for the agent's tenant.
    - `PUT /api/conversations/{id}/status` (Protected): Updates a conversation's status.
    - `POST /api/conversations/{id}/messages` (Public/Protected): Sends a message. The logic will differentiate between a visitor and an agent.
    - `GET /api/conversations/{id}/messages` (Public/Protected): Fetches message history.
- **Concurrency:** The new services will be designed to be thread-safe, leveraging the robust concurrency patterns established by the core services.
- **Logic:**
    - `ConversationService`: Will contain methods like `createConversation()`, `listConversations(tenantId)`, `updateConversationStatus(conversationId, status)`.
    - `MessageService`: Will contain methods like `sendMessage(conversationId, sender, text)`, `getMessageHistory(conversationId)`.

### 3.3. Database (Supabase)
- **Schema Changes:** This feature will introduce two new tables: `conversations` and `messages`.
    - `conversations` table: `id`, `tenant_id`, `status` (e.g., 'open', 'closed'), `created_at`.
    - `messages` table: `id`, `conversation_id`, `sender_id` (nullable for visitors), `sender_type` ('visitor' or 'agent'), `text`, `created_at`.
- **Data Access:** All queries in the new services MUST use the `DatabaseService` and be tenant-aware, upholding **Principle 2 (Strict Tenant Isolation)**. New RLS policies will be added to the `conversations` and `messages` tables to enforce this at the database level.

## 4. Performance Considerations

- Fetching message history will be paginated to ensure fast responses (<500ms) for conversations with thousands of entries.
- Listing conversations for a tenant will be paginated.

## 5. Out of Scope

- Real-time communication (e.g., WebSockets). This feature covers the RESTful API logic only.
- The user interface for the chat application.
- Notifications to agents or visitors.

## 6. User Scenarios & Acceptance Criteria

### Scenario 1: Visitor starts a chat
- **Given** an unauthenticated visitor on the website.
- **When** they send a message through the chat widget for the first time.
- **Then** the system creates a new conversation, associates it with the correct tenant (e.g., based on the website domain), and saves the first message.
- **Acceptance Criteria**:
  - [ ] A new record exists in the `conversations` table.
  - [ ] A new record exists in the `messages` table linked to the new conversation.

### Scenario 2: Agent lists conversations
- **Given** an authenticated agent.
- **When** they access the conversation dashboard.
- **Then** the system returns a list of conversations belonging only to their tenant.
- **Acceptance Criteria**:
  - [ ] The API response contains conversations only for the agent's `tenant_id`.
  - [ ] The API response does not contain conversations from other tenants.

### Scenario 3: Agent sends a reply
- **Given** an authenticated agent viewing a conversation.
- **When** they send a reply message.
- **Then** the system saves the message, associating it with the conversation and the agent's user ID.
- **Acceptance Criteria**:
  - [ ] A new record exists in the `messages` table with the correct `conversation_id` and `sender_id`.

## 7. Success Criteria
- **Performance**: P95 API response time for sending a message is < 300ms.
- **Performance**: P95 API response time for listing 100 conversations is < 500ms.
- **Reliability**: 99.9% of messages are successfully persisted.
- **Security**: Zero instances of cross-tenant data access are possible in the new endpoints.

## 8. Assumptions
- The core services from feature `002-backend-core-services` (`DatabaseService`, `JwtService`, `AuthFilter`) are available and will be used to handle authentication, authorization, and tenant-scoped database access.

## 9. Edge Cases
- A visitor attempts to send a message to a conversation that does not exist or belongs to another visitor.
- An agent attempts to access or send a message to a conversation outside of their assigned tenant.
- A message is sent to a conversation that is already 'closed'.
