# Implementation Tasks: Frontend UI Components

This document outlines the detailed, dependency-ordered tasks for implementing the Frontend UI Components feature. Tasks are grouped by logical phases, primarily aligned with user stories, to facilitate independent development and testing.

## Feature Summary

- **Feature Name**: Frontend UI Components
- **Objective**: Develop intuitive and functional user interfaces for visitor chat, agent dashboard, and system demonstration.
- **Suggested MVP Scope**: User Story 1 (Visitor Chat Interaction) and its foundational dependencies (Phase 1, Phase 2, Phase 3) and parts of the Demo Page (for integration example).

## Dependencies between User Stories

- **US1 (Visitor Chat Interaction)**: Independent.
- **US2 (Agent Conversation Management)**: Depends on US1 (to have conversations to manage).
- **US3 (Demo Page)**: Depends on US1 and US2 (to demonstrate their functionality).

## Parallel Execution Opportunities

Tasks marked with `[P]` can potentially be executed in parallel, assuming no direct file-level conflicts or specific internal dependencies not explicitly stated.

---

## Phase 1: Setup

**Goal**: Prepare the frontend environment and create necessary file structures for UI components.
**Independent Test Criteria**: N/A (Setup phase)

- [x] T001 Create `frontend/app/widget/` directory.
- [x] T002 Create `frontend/components/ChatWindow.tsx` file.
- [x] T003 Create `frontend/components/ConversationList.tsx` file.
- [x] T004 Create `frontend/app/widget/page.tsx` file.
- [x] T005 Update `frontend/app/dashboard/page.tsx` (already exists, will be enhanced).
- [x] T006 Update `frontend/app/page.tsx` (already exists, will be enhanced).

---

## Phase 2: Foundational (FR1: Widget UI)

**Goal**: Implement the core Widget UI components for visitor interaction.
**Independent Test Criteria**: Widget UI loads, displays a chat window, and allows composing messages.

- [x] T007 [US1] Implement chat window UI in `frontend/components/ChatWindow.tsx` (message display area).
- [x] T008 [US1] Implement message composer UI in `frontend/components/ChatWindow.tsx` (input field and send button).
- [x] T009 [US1] Implement basic state management within `frontend/components/ChatWindow.tsx` for messages and input.
- [x] T010 [US1] Implement widget shell (`app/widget/page.tsx`) to render `ChatWindow.tsx` and handle widget open/close state.
- [x] T011 [P] [US1] Create unit tests for `frontend/components/ChatWindow.tsx` covering UI rendering and message composition.
- [x] T012 [P] [US1] Create unit tests for `frontend/app/widget/page.tsx` covering widget shell rendering and state.

---

## Phase 3: User Story 1 (US1: Visitor Chat Interaction)

_As a **website visitor**, I want to **open a chat widget** and **send messages** to get support._
_Cross-cutting concern US4: chat widget functions without requiring login._

**Story Goal**: Enable visitors to open the chat widget, send messages, and initiate conversations.
**Independent Test Criteria**: Visitor can open the widget, type a message, and send it. The message should appear in the chat history, and a conversation should be created in the backend (mocked).

- [x] T013 [US1] Integrate `lib/api.ts` to send visitor messages from `frontend/components/ChatWindow.tsx`.
- [x] T014 [US1] Implement logic to create a new conversation via `lib/api.ts` if no active conversation exists for the visitor in `frontend/components/ChatWindow.tsx`.
- [x] T015 [US1] Implement display of messages fetched via `lib/api.ts` (e.g., initial history, or after sending).
- [x] T016 [P] [US1] Create integration tests for visitor chat flow: open widget, send message, verify API call (mocked).

---

## Phase 4: Foundational (FR2: Dashboard UI)

**Goal**: Implement the core Dashboard UI components for agent interaction.
**Independent Test Criteria**: Dashboard UI loads, displays a conversation list, and can view a selected conversation.

- [x] T017 [US2] Implement `ConversationList.tsx` to display a list of conversations for an agent.
- [x] T018 [US2] Implement logic to fetch agent's conversations using `lib/api.ts` in `frontend/components/ConversationList.tsx`.
- [x] T019 [US2] Implement UI to select a conversation from the list in `frontend/components/ConversationList.tsx`.
- [x] T020 [US2] Enhance `frontend/app/dashboard/page.tsx` to integrate `ConversationList.tsx` and display a selected conversation view.
- [x] T021 [US2] Implement agent reply interface (message composer) within the selected conversation view (potentially reusing `ChatWindow.tsx` components or logic).
- [x] T022 [P] [US2] Create unit tests for `frontend/components/ConversationList.tsx` covering rendering and selection logic.

---

## Phase 5: User Story 2 (US2: Agent Conversation Management)

_As an **agent**, I want to **view a list of active conversations** and **reply to visitor messages** through a dashboard._

**Story Goal**: Enable authenticated agents to view their conversations and reply to messages.
**Independent Test Criteria**: Authenticated agent can view their conversation list, select a conversation, see its message history, and send a reply.

- [x] T023 [US2] Integrate `lib/api.ts` to send agent replies from `frontend/app/dashboard/page.tsx` or a sub-component.
- [x] T024 [US2] Implement real-time polling for new messages in the selected conversation using `lib/api.ts` in `frontend/app/dashboard/page.tsx` or a sub-component.
- [x] T025 [P] [US2] Create integration tests for agent dashboard flow: authenticate, view list, select conversation, send reply, verify API call (mocked).

---

## Phase 6: Foundational (FR3: Demo Page)

**Goal**: Implement the Demo Page to showcase the system.
**Independent Test Criteria**: Demo page loads, displays system explanation, and provides embed code examples.

- [x] T026 [US3] Implement landing page content in `frontend/app/page.tsx` (system explanation, features).
- [x] T027 [US3] Implement embed code examples for the widget in `frontend/app/page.tsx`.
- [x] T028 [US3] Implement dynamic demonstration of multi-tenant capabilities (e.g., multiple widget instances/links) in `frontend/app/page.tsx`.
- [x] T029 [P] [US3] Create unit tests for `frontend/app/page.tsx` covering rendering of content and embed examples.

---

## Final Phase: Polish & Cross-Cutting Concerns

- [x] T030 Update `GEMINI.md` with new technologies added during this implementation (e.g., UI components).
- [x] T031 Ensure all new components/utilities adhere to project's linting and formatting rules.
- [x] T032 Review and update `README.md` or other documentation to reflect new frontend UI components.
- [x] T033 Implement basic styling using Tailwind CSS for all new UI components for a consistent look and feel.
- [x] T034 [P] Conduct E2E tests for the full visitor-to-agent chat flow across the widget and dashboard.

---

## Summary

- **Total task count**: 34
- **Task count per user story**:
  - Setup: 6
  - Foundational (FR1 - Widget UI): 6
  - US1 (Visitor Chat Interaction): 4
  - Foundational (FR2 - Dashboard UI): 6
  - US2 (Agent Conversation Management): 3
  - Foundational (FR3 - Demo Page): 4
  - Polish: 5
- **Parallel opportunities identified**: T011, T012, T016, T022, T025, T029, T034. (7 tasks)
- **Independent test criteria for each story**: Defined in `spec.md` Acceptance Criteria and within task descriptions.
- **Suggested MVP scope**: User Story 1 (Visitor Chat Interaction) and its foundational dependencies (Phase 1, Phase 2, Phase 3) and parts of the Demo Page (for integration example).

## Implementation Strategy

The feature will be implemented incrementally, prioritizing user stories based on their dependencies. Each user story phase is designed to be a shippable increment, allowing for continuous integration and testing. Development will proceed in the following order:

1.  **Phase 1: Setup**
2.  **Phase 2: Foundational (FR1 - Widget UI)**
3.  **Phase 3: User Story 1 (Visitor Chat Interaction)**
4.  **Phase 4: Foundational (FR2 - Dashboard UI)**
5.  **Phase 5: User Story 2 (Agent Conversation Management)**
6.  **Phase 6: Foundational (FR3 - Demo Page)**
7.  **Final Phase: Polish & Cross-Cutting Concerns**
