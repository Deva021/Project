# Feature Specification: REST API Servlets

## 1. Problem Statement

Users of the chat application need a robust and well-defined set of API endpoints to interact with the backend services. Without these, the frontend cannot perform essential operations such as checking system health, managing conversations, sending/receiving messages, and establishing real-time communication. This feature aims to provide these foundational API capabilities for the chat application.

## 2. User Scenarios & Testing

### User Stories

- As a system administrator, I want to quickly verify the health of the chat backend.
- As a visitor, I want to initiate a new conversation via the API.
- As an agent, I want to view a list of all active conversations within my tenant.
- As a visitor, I want to send messages to an ongoing conversation.
- As an agent, I want to send replies to a visitor's message.
- As an agent, I want to retrieve the history of messages for a specific conversation.
- As a client application, I want to receive real-time updates and events from the backend without continuously polling.

### Acceptance Criteria

- **Scenario: Health Check Success**
  - GIVEN the backend services are operational
  - WHEN a request is made for system health status
  - THEN the system indicates an operational status.

- **Scenario: Create Public Conversation**
  - GIVEN a visitor provides initial conversation data
  - WHEN a request to create a conversation is processed
  - THEN a new conversation is created
  - AND the system confirms creation.

- **Scenario: List Tenant Conversations**
  - GIVEN an authenticated agent
  - WHEN a request for conversations is made
  - THEN the system returns a list of conversations pertinent to the agent's tenant.

- **Scenario: Send Visitor Message**
  - GIVEN an active conversation
  - WHEN a visitor sends a message
  - THEN the message is recorded in the conversation
  - AND the system confirms successful delivery.

- **Scenario: Send Agent Message**
  - GIVEN an authenticated agent and an active conversation
  - WHEN an agent sends a message
  - THEN the message is recorded in the conversation
  - AND the system confirms successful delivery.

- **Scenario: Fetch Message History**
  - GIVEN an authenticated agent and a specific conversation
  - WHEN a request is made for message history
  - THEN the system returns the chronological list of messages for that conversation.

- **Scenario: Real-time Event Polling**
  - GIVEN a client waiting for real-time events
  - WHEN a long-polling request is initiated
  - THEN the system holds the connection until an event occurs or a timeout is reached
  - AND delivers any new events relevant to the client.

## 3. Proposed Solution

This feature will implement the core REST API for the chat application through four distinct Java servlets, each responsible for a specific domain of functionality. These servlets will handle HTTP requests for health checks, conversation management, message operations, and real-time event polling.

### Servlets Included:
-   **HealthServlet**: Provides a simple endpoint to verify the operational status of the backend.
-   **ConversationsServlet**: Manages chat conversations, allowing for creation and retrieval.
-   **MessagesServlet**: Handles the sending and fetching of messages within conversations.
-   **PollServlet**: Implements a long-polling mechanism for real-time updates and event delivery to clients.

## 4. Functional Requirements

### FR1: Health Check Endpoint
- The system must provide a public mechanism to check the operational status of the backend.

### FR2: Conversation Management Endpoints
- The system must provide a public mechanism to create new conversations.
- The system must provide a protected mechanism to list conversations, scoped by the user's tenant.

### FR3: Message Management Endpoints
- The system must provide a mechanism to send messages, supporting both public (visitor) and protected (agent) message submission.
- The system must provide a protected mechanism to retrieve message history for a given conversation, scoped by the user's tenant.

### FR4: Real-time Event Polling Endpoint
- The system must provide a protected mechanism that supports long-polling for real-time event delivery.
- This polling mechanism must integrate with an event queue.
- This polling mechanism must handle connection timeouts gracefully.

## 5. Success Criteria

- System administrators can confirm backend operational status within 50ms 100% of the time.
- New conversations can be created successfully 100% of the time.
- Authenticated agents can retrieve their tenant's conversations within 200ms 95% of the time.
- Messages (visitor and agent) are successfully sent and recorded 100% of the time.
- Authenticated agents can retrieve message history within 200ms 95% of the time.
- Clients receive relevant real-time events within 1 second of occurrence, or the polling connection times out gracefully after 30 seconds if no events are present.
- The API layer successfully handles 50 concurrent requests for non-polling endpoints without degradation in response times.

## 6. Technical Design

### 6.1. Frontend (Next.js)
-   **Components:** This specification focuses on the backend API; no new Next.js components are directly introduced. Frontend components will consume these APIs.
-   **State Management:** N/A (Backend focused)
-   **Data Fetching:** N/A (Backend focused)

### 6.2. Backend (Java Servlets)
-   **API Endpoints:**
    -   `/health` (GET): Simple status check.
    -   `/conversations` (POST): Create a new conversation (public).
    -   `/conversations` (GET): List conversations for a tenant (protected).
    -   `/messages` (POST): Send a message (public for visitors, protected for agents).
    -   `/messages` (GET): Fetch message history for a conversation (protected).
    -   `/poll` (GET): Long-polling endpoint for real-time events (protected).
-   **Concurrency:** Each servlet's `doGet` or `doPost` method will handle requests in a thread-safe manner. The `PollServlet` in particular will need to manage long-lived connections efficiently, potentially using `BlockingQueue` for integration with real-time event producers and `ExecutorService` for managing worker threads to process events, adhering to **Principle 3 (Robust Concurrency)**.
-   **Logic:**
    -   **HealthServlet:** Return a simple success status (e.g., HTTP 200 OK) with a basic payload.
    -   **ConversationsServlet:** Delegate to `ConversationService` for creating and listing conversations. Listing will require authentication and tenant context.
    -   **MessagesServlet:** Delegate to `MessageService` for sending and fetching messages. Sending public messages might not require prior authentication (e.g., initial visitor message), but agent replies and fetching history will be protected.
    -   **PollServlet:** Integrate with a queuing mechanism (e.g., `PollQueueService` from previous specs) to hold client connections until events are available or a timeout occurs.

### 6.3. Database (Supabase)
-   **Schema Changes:** No new schema changes are introduced by this feature. All servlets will interact with the existing `tenants`, `conversations`, `messages` tables via their respective services.
-   **Data Access:** All protected API endpoints will rely on the `AuthFilter` and `TenantContext` to ensure **Principle 2 (Strict Tenant Isolation)** is enforced. Database interactions will use services that inherently apply tenant scoping (e.g., RLS in Supabase, explicit `tenant_id` filtering in service logic).

## 7. Performance Considerations

This feature will adhere to **Principle 4 (High-Performance User Experience)** by ensuring efficient handling of API requests.
-   **Estimated API response times:**
    -   `/health`: < 50ms (very low overhead)
    -   `/conversations` (POST/GET): < 200ms (typical database interaction)
    -   `/messages` (POST/GET): < 200ms (typical database interaction)
    -   `/poll`: Up to 30s (due to long-polling design, but immediate response if event is present)
-   **Impact on client bundle size:** Minimal to none, as this is a backend-focused feature.

## 8. Key Entities & Data Model

-   **Conversation**: A chat session, tied to a tenant.
-   **Message**: A single unit of communication within a conversation.
-   **Tenant**: The organizational unit to which conversations and messages belong.
-   **User**: An individual (agent or visitor) interacting with the system.

## 9. Edge Cases

-   **Invalid Input**: Requests with missing or malformed parameters (e.g., invalid conversation ID, empty message content) should result in appropriate error responses (e.g., HTTP 400 Bad Request).
-   **Unauthorized Access**: Attempts to access protected resources without valid authentication or for a tenant the user does not belong to should result in HTTP 401 Unauthorized or HTTP 403 Forbidden responses.
-   **System Overload**: Under extreme load, servlets should handle backpressure gracefully (e.g., PollServlet's queue integration) to prevent system crashes, returning appropriate error codes (e.g., HTTP 503 Service Unavailable) if resources are exhausted.
-   **No Events for Polling**: The PollServlet must handle cases where no events are available for the client within the timeout period, resulting in a successful timeout response (e.g., HTTP 200 OK with an empty or status payload).

## 10. Assumptions

-   Existence of `ConversationService`, `MessageService`, `PollQueueService`, `AuthFilter`, `TenantContext`, and `JwtService` from prior specifications.
-   Frontend applications will handle parsing and display of JSON responses from these APIs.
-   Error responses will primarily use HTTP status codes, with minimal JSON bodies for simple error messages.

## 11. Out of Scope

-   Direct implementation of `ConversationService`, `MessageService`, or `PollQueueService` logic.
-   Frontend UI for interacting with these API endpoints.
-   WebSocket-based real-time communication (PollServlet uses long-polling).
-   Detailed error codes beyond standard HTTP status codes.
-   User authentication and authorization mechanisms (covered by `AuthFilter` and `JwtService`).