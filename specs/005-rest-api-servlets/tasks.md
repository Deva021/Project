# Tasks for REST API Servlets

## Dependencies

The user stories are largely independent and can be implemented in parallel, with the exception of those sharing the same servlet file. The order below is a logical progression.

-   **US1** (Health Check) is independent.
-   **US2** and **US3** share `ConversationsServlet` but can be worked on sequentially.
-   **US4**, **US5**, and **US6** share `MessagesServlet` and can be worked on sequentially.
-   **US7** (Polling) is independent.

## Parallel Execution

-   The work on `HealthServlet`, `ConversationsServlet`, `MessagesServlet`, and `PollServlet` can be started in parallel.
-   Within each servlet, the implementation of different HTTP methods (e.g., `doGet`, `doPost`) can be done in parallel if they don't have overlapping logic.
-   Unit tests for each servlet can be written in parallel with their implementation.

## Implementation Strategy

The implementation will follow an MVP-first approach. The initial focus will be on delivering the core functionality for each user story independently.

---

## Phase 1: Setup

*No specific setup tasks are required for this feature as it builds upon the existing project structure.*

---

## Phase 2: Foundational Tasks

*No foundational tasks are required. All tasks are tied to specific user stories.*

---

## Phase 3: User Story 1 - Health Check

**Goal:** As a system administrator, I want to quickly verify the health of the chat backend.
**Independent Test Criteria:** A GET request to `/health` returns a 200 OK status with a JSON payload indicating "OK".

- [x] T001 Create the `HealthServlet.java` file in `backend/src/main/java/com/minintercom/servlets/`.
- [x] T002 Implement the `doGet` method in `HealthServlet.java` to return a 200 OK status and a simple JSON response (e.g., `{"status": "OK"}`).
- [x] T003 Create `HealthServletTest.java` in `backend/src/test/java/com/minintercom/servlets/`.
- [x] T004 [P] [US1] Write a unit test for `HealthServlet.doGet` to verify it returns the correct status and payload.

---

## Phase 4: User Story 2 & 3 - Conversation Management

**Goal:** As a visitor, I want to initiate a new conversation via the API. As an agent, I want to view a list of all active conversations within my tenant.
**Independent Test Criteria:** A POST request to `/conversations` with valid data creates a new conversation. A GET request to `/conversations` by an authenticated agent returns a list of conversations for their tenant.

- [x] T005 Create the `ConversationsServlet.java` file in `backend/src/main/java/com/minintercom/servlets/`.
- [x] T006 [US2] Implement the `doPost` method in `ConversationsServlet.java` to handle conversation creation.
- [x] T007 [US3] Implement the `doGet` method in `ConversationsServlet.java` to handle listing conversations for a tenant.
- [x] T008 Create `ConversationsServletTest.java` in `backend/src/test/java/com/minintercom/servlets/`.
- [x] T009 [P] [US2] Write a unit test for `ConversationsServlet.doPost`.
- [x] T010 [P] [US3] Write a unit test for `ConversationsServlet.doGet`.

---

## Phase 5: User Story 4, 5, & 6 - Message Management

**Goal:** As a visitor or agent, I want to send messages. As an agent, I want to retrieve message history.
**Independent Test Criteria:** A POST request to `/messages` with valid data adds a message to a conversation. A GET request to `/messages` with a valid conversation ID returns the message history.

- [x] T011 Create the `MessagesServlet.java` file in `backend/src/main/java/com/minintercom/servlets/`.
- [x] T012 [US4][US5] Implement the `doPost` method in `MessagesServlet.java` to handle sending messages.
- [x] T013 [US6] Implement the `doGet` method in `MessagesServlet.java` to handle fetching message history.
- [x] T014 Create `MessagesServletTest.java` in `backend/src/test/java/com/minintercom/servlets/`.
- [x] T015 [P] [US4][US5] Write a unit test for `MessagesServlet.doPost`.
- [x] T016 [P] [US6] Write a unit test for `MessagesServlet.doGet`.

---

## Phase 6: User Story 7 - Real-time Polling

**Goal:** As a client application, I want to receive real-time updates and events from the backend without continuously polling.
**Independent Test Criteria:** A GET request to `/poll` holds the connection open until an event is available or a timeout is reached.

- [x] T017 Create the `PollServlet.java` file in `backend/src/main/java/com/minintercom/servlets/`.
- [x] T018 [US7] Implement the `doGet` method in `PollServlet.java` to handle long-polling requests.
- [x] T019 Create `PollServletTest.java` in `backend/src/test/java/com/minintercom/servlets/`.
- [x] T020 [P] [US7] Write a unit test for `PollServlet.doGet` to verify its long-polling behavior.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [x] T021 Add JavaDoc comments to all new servlets and public methods.
- [x] T022 Review and update the `web.xml` file in `backend/src/main/webapp/WEB-INF/` to include mappings for all new servlets.
- [x] T023 Create integration tests for the servlet filter chain to ensure protected endpoints are correctly secured.
