# REST API Servlets - Implementation Plan

## 1. Objective

The high-level goal of this feature is to implement the core REST API for the chat application. This involves creating four distinct Java servlets to handle health checks, conversation management, message handling, and real-time event polling, providing the foundational backend endpoints for the application.

## 2. Constitution Checklist

- [ ] **Principle 1: Server Components by Default:** Not applicable. This is a backend-only feature.
- [x] **Principle 2: Strict Tenant Isolation:** Yes. The plan requires all protected endpoints to be secured by the existing `AuthFilter`, which uses `TenantContext` to enforce tenant-scoped access.
- [x] **Principle 3: Robust Concurrency:** Yes. The `PollServlet` implementation will use thread-safe patterns (e.g., `BlockingQueue`) for managing long-polling connections and events. Other servlets will be designed to be thread-safe in a standard servlet container environment.
- [x] **Principle 4: High-Performance UX:** Yes. The implementation will aim for the performance targets outlined in the specification (<200ms for standard API calls).
- [x] **Principle 5: Specification-First:** Yes. This plan is derived directly from the approved `specs/005-rest-api-servlets/spec.md`.

## 3. Implementation Steps

| Step | Description | Owner | Status |
| :--- | :---------- | :---- | :----- |
| 1.   | Implement `HealthServlet.java` for basic health checks. | AI Agent | To Do  |
| 2.   | Implement `ConversationsServlet.java` for creating and listing conversations. | AI Agent | To Do  |
| 3.   | Implement `MessagesServlet.java` for sending and retrieving messages. | AI Agent | To Do  |
| 4.   | Implement `PollServlet.java` for long-polling event delivery. | AI Agent | To Do  |
| 5.   | Add unit tests for all four servlets with mocked services. | AI Agent | To Do  |
| 6.   | Add integration tests to verify servlet functionality and filter chain integration. | AI Agent | To Do  |

## 4. Testing Strategy

- **Unit Tests:** Each servlet (`HealthServlet`, `ConversationsServlet`, `MessagesServlet`, `PollServlet`) will be unit-tested in isolation. The tests will use mock objects (e.g., via Mockito) for `HttpServletRequest`, `HttpServletResponse`, and the underlying services (`ConversationService`, `MessageService`, `PollQueueService`) to verify that each servlet correctly handles requests, delegates to services, and produces the expected responses.
- **Integration Tests:** Integration tests will be set up to start an embedded web server (e.g., Tomcat or Jetty) with the servlets and the `AuthFilter` deployed. These tests will make real HTTP requests to the endpoints to verify the full request/response cycle, including filter chain behavior, tenant isolation, and correct servlet routing.
