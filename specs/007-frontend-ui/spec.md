# Feature Specification: Frontend UI Components

## 1. Problem Statement

A live chat system requires intuitive and functional user interfaces for both visitors and agents, along with a clear demonstration of its capabilities. Without these UI components, the core functionality of the chat system remains inaccessible and undemonstrated. This feature aims to deliver the essential user-facing elements, enabling visitors to engage, agents to manage conversations, and potential users to understand the system's value.

## 2. User Scenarios & Testing

### User Stories

- As a **website visitor**, I want to **open a chat widget** and **send messages** to get support.
- As an **agent**, I want to **view a list of active conversations** and **reply to visitor messages** through a dashboard.
- As a **potential user/developer**, I want to **see a demo page** that explains the system and provides **embed code examples** for integration.
- As a **website visitor**, I want the chat widget to **function without requiring login**.

### Acceptance Criteria

- **Scenario: Visitor Initiates Chat**
  - GIVEN a visitor is on a website with the embedded widget
  - WHEN they open the widget and type a message
  - THEN the message is sent to the backend, and a new conversation is created if none exists.

- **Scenario: Agent Manages Conversation**
  - GIVEN an authenticated agent is on the dashboard
  - WHEN they select a conversation from the list
  - THEN the messages for that conversation are displayed, and they can type and send a reply.

- **Scenario: Demo Page Provides Embed Instructions**
  - GIVEN a user visits the demo page
  - WHEN they view the page content
  - THEN they see an explanation of the system, embed code examples for different tenants, and a live demonstration of the widget.

## 3. Proposed Solution

The solution involves implementing three main frontend UI components: the Chat Widget for visitors, the Agent Dashboard for managing conversations, and a Demo Page to showcase the system's multi-tenant capabilities. These components will utilize the previously established frontend core (Supabase Client, API Client, Auth Context) to provide authentication, API communication, and state management.

## 4. Functional Requirements

### FR1: Widget UI
- The system must provide a chat window component (`ChatWindow.tsx`) to display messages.
- The system must provide a message composer for visitors to type and send messages.
- The Widget UI must operate in visitor mode, allowing full functionality without requiring explicit user authentication for the visitor.
- The Widget UI must be rendered via `app/widget/page.tsx`.

### FR2: Dashboard UI
- The system must provide a conversation list component (`ConversationList.tsx`) to display active conversations for an authenticated agent.
- The system must display a selected conversation view, showing message history.
- The system must provide an agent reply interface for sending messages within a conversation.
- The Dashboard UI must require agent authentication through the `AuthContext`.
- The Dashboard UI must be rendered via `app/dashboard/page.tsx`.

### FR3: Demo Page
- The system must implement a landing page (`app/page.tsx`) that serves as a demonstration of the overall system.
- The Demo Page must include clear embed code examples for integrating the chat widget into external websites.
- The Demo Page must demonstrate the multi-tenant capabilities of the system (e.g., showing multiple widget instances for different tenants).

## 5. Success Criteria

- 95% of visitors can successfully send a message through the widget within 5 seconds of opening it.
- Agents can view, select, and reply to conversations with an average response time of under 2 seconds for UI updates.
- 100% of the key features demonstrated on the demo page are functional and clearly explained.
- The UI components maintain a consistent and responsive user experience across common desktop and mobile browser sizes.

## 6. Technical Design

### 6.1. Frontend (Next.js)
- **Components:** `app/widget/page.tsx`, `ChatWindow.tsx`, `app/dashboard/page.tsx` (existing, will be enhanced), `ConversationList.tsx`, `app/page.tsx`. Adheres to **Principle 1 (Server Components)** by using Server Components where appropriate for static content and initial data fetching.
- **State Management:** Local UI state within components will use `useState`/`useReducer`. Global authentication state will be managed by `AuthContext`. Conversation and message state will be managed locally within the dashboard/widget components, potentially using a global store if complexity increases.
- **Data Fetching:** Data will be fetched using the `lib/api.ts` client. Server Components will leverage this client for server-side data fetching of initial data (e.g., conversation lists). Client Components will handle real-time updates and interactive data submission.

### 6.2. Backend (Java Servlets / TCP Relay)
- **API Endpoints:** The existing backend API endpoints (`/api/conversations`, `/api/messages`, `/api/poll`) will be consumed by the frontend UI components.
- **Concurrency:** Not directly impacted by frontend UI changes.
- **Logic:** The frontend UI components will interact with existing backend business logic via the defined API endpoints for creating conversations, sending messages, and polling for updates.

### 6.3. Database (Supabase)
- **Schema Changes:** No direct schema changes from this frontend UI feature.
- **Data Access:** All user authentication data access will be handled by Supabase Auth service (via `AuthContext`). Application data will be accessed via the backend API endpoints. All access patterns MUST adhere to **Principle 2 (Strict Tenant Isolation)**, implicitly enforced by the backend API and Supabase RLS.

## 7. Performance Considerations

*How will this feature meet the requirements of **Principle 4 (High-Performance UX)**?*
- Estimated API response times for chat interactions should be under 500ms to maintain a responsive feel.
- Initial load time for the widget and dashboard pages should target LCP < 2.5 seconds.
- The client-side JavaScript bundle size for the widget should be minimized to avoid impacting host website performance.
- UI components will be optimized for efficient rendering to prevent jank.

## 8. Key Entities & Data Model

- **Conversation**: Represents a chat thread between a visitor and agents. (Defined in data-model.md of 006-frontend-core)
- **Message**: Represents an individual chat message within a conversation. (Defined in data-model.md of 006-frontend-core)
- **Visitor**: An unauthenticated user interacting with the widget.
- **Agent**: An authenticated user managing conversations via the dashboard.
- **Tenant**: Represents a business, providing context for conversations and agent access.

## 9. Assumptions

- Frontend Core Components (Supabase Client, API Client, Auth Context) are fully implemented and functional.
- Backend API endpoints (`/api/conversations`, `/api/messages`, `/api/poll`) are operational and accessible.
- Supabase/Auth Context provides reliable authentication and session management.
- A CSS framework (e.g., Tailwind CSS) is available and configured for styling the UI components.
- The `tenant_public_key` mechanism for identifying tenants in the widget is supported by the backend.

## 10. Out of Scope

- Implementing advanced chat features like file uploads, emojis, or read receipts.
- Real-time updates via WebSockets (long-polling is assumed as per backend design).
- Full user management or agent management UI within the dashboard.
- Complex routing or transfer of conversations between agents.
- Detailed visual design system or extensive customization options for the widget beyond basic theming.

