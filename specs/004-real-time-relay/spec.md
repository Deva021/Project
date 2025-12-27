# Feature Specification: Real-Time Relay

## 1. Problem Statement

The application's current chat system lacks real-time communication capabilities, leading to delays in message delivery and agent responsiveness. This hinders efficient customer support and a seamless user experience.

## 2. Proposed Solution

This feature introduces a comprehensive real-time layer to enable instant message exchange and live updates within conversations. This layer will consist of a TCP-based Relay Server, a Relay Router for intelligent event distribution, a Relay Client for secure backend integration, and a Poll Queue Service for efficient, scalable event delivery to diverse client types (e.g., web agents using long-polling). This unified approach will replace traditional RESTful polling for real-time events, ensuring immediate feedback and improved agent productivity.

## 3. Technical Design

### 3.1. Frontend (Next.js)
- **Components:** N/A. This feature primarily focuses on the backend real-time communication infrastructure. Frontend integration will be addressed in subsequent features.
- **State Management:** N/A.
- **Data Fetching:** N/A.

### 3.2. Backend Components
- **API Endpoints:**
    - The Real-Time Layer will primarily communicate via a custom real-time protocol, distinct from traditional REST API endpoints.
    - The Relay Client will be a backend component, integrating with existing chat services to publish real-time events to the Relay Server.
    - The Poll Queue Service will expose an internal API for clients (e.g., web frontend agents) to long-poll for events.
- **Concurrency:**
    - **Relay Server**: Configured with a scalable thread pool to efficiently handle numerous concurrent TCP connections. Each client connection will be managed by a dedicated client handler.
    - **Relay Router**: Designed for robust, thread-safe event routing and dynamic subscriber management, ensuring high throughput and reliable delivery.
    - **Poll Queue Service**: Utilizes `BlockingQueue` mechanisms per tenant and conversation to manage event queues, supporting long-poll requests with configurable timeouts. Adheres to **Principle 3 (Robust Concurrency)**.
- **Logic:**
    - **Relay Server Protocol Specification**: Defines the custom TCP message format, including mechanisms for client connection establishment, disconnection handling, and standardized event types (e.g., `new_message`, `agent_typing`, `conversation_status_update`). This will be documented as a separate protocol design document.
    - **Relay Server Implementation**: The server component will be responsible for accepting incoming client connections, managing a pool of resources for processing requests, and employing dedicated handlers for each client's lifecycle and communication.
    - **Relay Router Specification**: Manages event routing logic based on tenant ID and conversation ID, dynamically registers and deregisters subscribers, and implements broadcast mechanisms for events relevant to multiple recipients.
    - **Relay Client Specification**: A backend component responsible for connecting to the Real-Time Layer, publishing events from the core application services, and implementing resilient connection retry logic to ensure continuous operation.
    - **Poll Queue Service Specification**: Manages queues of real-time events for each tenant and conversation, provides an interface for long-polling clients to retrieve events, and handles timeouts for pending requests.

### 3.3. Database (Supabase)
- **Schema Changes:** No direct schema changes are required for the real-time relay infrastructure itself. The relay layer will interact with existing chat and tenant data models, pulling information from the database as needed to determine event routing and access permissions.
- **Data Access:** Data access will primarily occur via existing services (e.g., `ConversationService`, `MessageService`) which already adhere to **Principle 2 (Strict Tenant Isolation)**. The Relay Client will leverage these services, inheriting their tenant-aware data access.

## 4. Performance Considerations

This feature is designed to meet the requirements of **Principle 4 (High-Performance UX)** by providing near real-time communication.
- **Event Latency**: P95 latency for a `new_message` event from the backend system to a subscribed agent should be less than 200ms under normal load.
- **Concurrent Connections**: The Relay Server must efficiently handle at least 10,000 concurrent client connections without significant degradation in performance.
- **Scalability**: The entire real-time layer should be designed to scale horizontally to accommodate increased load and event volume.
- **Resource Usage**: Long-polling in the Poll Queue Service will be optimized to prevent excessive CPU or memory consumption on both server and client.

## 5. Out of Scope

- **Frontend UI Development**: This specification does not include the implementation of specific UI components in the Next.js frontend to consume real-time events.
- **Event Persistence**: The Relay Server and Poll Queue Service are primarily for real-time event delivery, not for long-term storage of events. Events will be published from persistent data sources (e.g., database-backed chat messages).
- **Advanced Real-Time Features**: Features like message read receipts, complex presence management (beyond basic agent online/offline), or direct client-to-client messaging are out of scope for this initial implementation.

## 6. User Scenarios & Acceptance Criteria

### Scenario 1: Visitor sends a new message to a conversation

- **Given** a visitor sends a new message through the frontend chat widget.
- **When** the backend chat service successfully processes and stores the message in the database.
- **Then** the Relay Client publishes a `new_message` event to the Relay Server, targeting the relevant conversation and tenant.
- **Acceptance Criteria**:
  - [ ] The `new_message` event is correctly formatted according to the Relay Server Protocol.
  - [ ] The Relay Server receives the event and routes it to all subscribed agents for that conversation/tenant.
  - [ ] Agents connected via the Relay Server (or long-polling the Poll Queue Service) receive the `new_message` event with minimal latency.

### Scenario 2: Agent starts typing in a conversation

- **Given** an agent is actively viewing a conversation in their dashboard and begins typing a message.
- **When** the frontend detects the agent typing event.
- **Then** the frontend (via the existing backend or a dedicated client) publishes an `agent_typing` event to the Relay Server.
- **Acceptance Criteria**:
  - [ ] The `agent_typing` event is correctly routed to the visitor and other subscribed agents for that conversation.
  - [ ] The event is transient and does not require persistence.

### Scenario 3: Agent receives conversation status update

- **Given** a conversation's status changes (e.g., from 'OPEN' to 'ACTIVE' by another agent).
- **When** the backend chat service updates the conversation status in the database.
- **Then** the Relay Client publishes a `conversation_status_update` event to the Relay Server.
- **Acceptance Criteria**:
  - [ ] Subscribed agents (e.g., in a conversation list view) receive the status update event.
  - [ ] The updated status is reflected in their respective UIs.

## 7. Functional Requirements

-   The real-time server component MUST establish and manage persistent connections with multiple clients.
-   The real-time server component MUST handle graceful and ungraceful client disconnections and reconnections.
-   The real-time server component MUST parse incoming messages according to the defined real-time protocol and dispatch them to the event routing component.
-   The event routing component MUST maintain a dynamic registry of active subscribers (clients/agents) per tenant and conversation.
-   The event routing component MUST intelligently route events (e.g., \`new_message\`, \`agent_typing\`, \`conversation_status_update\`) to the correct set of subscribed clients.
-   The event routing component MUST support broadcasting events to all subscribers within a tenant or specific conversation.
-   The backend client component MUST connect to the real-time server component and publish events from the main application logic.
-   The Relay Client MUST implement robust connection retry and backoff mechanisms.
-   The event queue service MUST provide isolated event queues for different tenants and conversations.
-   The event queue service MUST support long-polling requests from clients, holding the connection open until an event is available or a timeout occurs.
-   The event queue service MUST notify connected clients when new events are pushed to their subscribed queues.

## 8. Success Criteria

-   **Performance (Latency)**: P95 latency for all real-time events (from backend publication to client reception) is consistently below 200ms under normal load.
-   **Performance (Scale)**: The Relay Server is capable of maintaining 10,000 concurrent client connections with stable performance.
-   **Reliability**: 99.99% of all published real-time events are successfully delivered to all intended and subscribed clients.
-   **Availability**: The real-time layer (Relay Server, Router, Poll Queue Service) maintains 99.9% uptime.
-   **Accuracy**: Events are routed only to relevant subscribers (correct tenant and conversation scope).

## 9. Assumptions

-   Existing authentication and authorization mechanisms (`JwtService`, `AuthFilter`) will secure access to protected real-time channels and validate event publishers/subscribers.
-   `TenantContext` will be correctly utilized by the Relay Client and Poll Queue Service to enforce tenant isolation for all event operations.
-   The Relay Server and Poll Queue Service will be deployed in an environment that allows for persistent TCP connections and efficient long-polling (e.g., appropriate server configurations, load balancers).
-   The backend services (e.g., `ConversationService`, `MessageService`) will be responsible for triggering the Relay Client to publish events after data changes.

## 10. Edge Cases

-   **Client Disconnection**: Handling of abrupt and graceful client disconnections from the Relay Server.
-   **Network Partitions**: Behavior when the Relay Client or Relay Server experiences temporary network outages.
-   **High Event Volume**: Performance and stability when a large number of events are published simultaneously for a single conversation or tenant.
-   **Unsubscribed Clients**: Events published to conversations with no active subscribers.
-   **Long-Poll Timeouts**: Graceful handling of long-poll requests that time out without any events.
-   **Event Reordering**: Although TCP ensures order, ensure logical event processing accounts for potential delays or out-of-order processing if multiple sources push to the same queue.
