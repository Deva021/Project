# Implementation Tasks: Frontend Core Components

This document outlines the detailed, dependency-ordered tasks for implementing the Frontend Core Components feature. Tasks are grouped by logical phases, primarily aligned with user stories, to facilitate independent development and testing.

## Feature Summary
- **Feature Name**: Frontend Core Components
- **Objective**: Establish a solid and reusable frontend foundation for Supabase client integration, robust API communication, and global authentication state management.
- **Suggested MVP Scope**: User Story 1 (Sign In) and its foundational dependencies.

## Dependencies between User Stories
- **US1 (Sign In)**: Independent.
- **US2 (Sign Out)**: Depends on US1 (for an authenticated user to sign out).
- **US3 (Session Management)**: Depends on US1 (to have a session to manage).
- **US4 (Secure API Calls)**: Depends on US3 (to get authentication tokens from a managed session).

## Parallel Execution Opportunities
Tasks marked with `[P]` can potentially be executed in parallel, assuming no direct file-level conflicts or specific internal dependencies not explicitly stated.

---

## Phase 1: Setup

**Goal**: Prepare the frontend environment and create necessary file structures.
**Independent Test Criteria**: N/A (Setup phase)

- [x] T001 Create `lib` directory in `frontend/`.
- [x] T002 Create `frontend/lib/supabase.ts` file.
- [x] T003 Create `frontend/lib/api.ts` file.
- [x] T004 Create `frontend/components/AuthContext.tsx` file.
- [x] T005 Ensure `.env.local` contains Supabase environment variables (`NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`).

---

## Phase 2: Foundational (FR1: Supabase Client Utility)

**Goal**: Implement the core Supabase client initialization and basic authentication helpers.
**Independent Test Criteria**: Supabase client initializes correctly, `getSession`, `signIn`, and `signOut` functions are callable and behave as expected for basic scenarios.

- [x] T006 Implement `createClient()` for browser-side Supabase in `frontend/lib/supabase.ts`.
- [x] T007 Implement `getSession()` in `frontend/lib/supabase.ts`.
- [x] T008 Implement `signIn(email, password)` in `frontend/lib/supabase.ts`.
- [x] T009 Implement `signOut()` in `frontend/lib/supabase.ts`.
- [x] T010 Add necessary types for Supabase session and user to `frontend/lib/supabase.ts`.
- [x] T011 [P] Create unit tests for `frontend/lib/supabase.ts` covering client initialization and auth helpers.

---

## Phase 3: User Story 1 (US1: Sign In)

**Story Goal**: As an unauthenticated user, I want to sign in to access my application features.
**Independent Test Criteria**: Users can successfully sign in with valid credentials, their session is established, and they are redirected to the dashboard.
**Acceptance Criteria**:
- Scenario: Successful User Sign-In
  - GIVEN a user is on the login page
  - WHEN they enter valid credentials and submit
  - THEN their session is established, and they are redirected to the dashboard.

- [x] T012 [US1] Implement `login` function in `frontend/components/AuthContext.tsx` using `lib/supabase.ts`'s `signIn` helper.
- [x] T013 [P] [US1] Create basic login page `frontend/app/login/page.tsx` that uses the `AuthContext`'s `login` function.
- [x] T014 [US1] Implement post-login redirection in `frontend/components/AuthContext.tsx`.
- [x] T015 [P] [US1] Create integration tests for sign-in flow, verifying session establishment and redirection.

---

## Phase 4: User Story 2 (US2: Sign Out)

**Story Goal**: As an authenticated user, I want to sign out securely.
**Independent Test Criteria**: Authenticated users can successfully sign out, their session is terminated, and they are redirected to the login page or equivalent.
**Acceptance Criteria**: N/A (implied by US1 and AuthContext usage)

- [x] T016 [US2] Implement `logout` function in `frontend/components/AuthContext.tsx` using `lib/supabase.ts`'s `signOut` helper.
- [x] T017 [P] [US2] Add logout button to a sample authenticated page (e.g., `frontend/app/dashboard/page.tsx`) that triggers the `AuthContext`'s `logout` function.
- [x] T018 [US2] Implement post-logout redirection in `frontend/components/AuthContext.tsx`.
- [x] T019 [P] [US2] Create integration tests for sign-out flow, verifying session termination and redirection.

---

## Phase 5: User Story 3 (US3: Session Management)

**Story Goal**: As an authenticated user, I want the application to maintain my session across page navigations and refreshes.
**Independent Test Criteria**: User sessions persist across browser restarts and page refreshes, and the application's authentication state (`isAuthenticated`, `user`) accurately reflects the current session status.
**Acceptance Criteria**:
- Scenario: Automatic Session Re-establishment
  - GIVEN an authenticated user closes and re-opens the browser
  - WHEN they navigate to the application
  - THEN their session is automatically re-established, and they remain logged in.

- [x] T020 [US3] Implement initial session loading and state management (`session`, `user`, `isLoading`) within `frontend/components/AuthContext.tsx` using `lib/supabase.ts`'s `getSession`.
- [x] T021 [US3] Ensure `AuthContext` automatically re-establishes session on app load/refresh (e.g., using `useEffect` with `getSession`).
- [x] T022 [P] [US3] Create integration tests for session persistence across page loads/refreshes.
- [x] T023 [US3] Wrap the application root (`frontend/app/layout.tsx`) with the `AuthProvider` from `frontend/components/AuthContext.tsx`.

---

## Phase 6: User Story 4 (US4: Secure API Calls)

**Story Goal**: As an application component, I want to make secure API calls to the backend without manually handling tokens or errors.
**Independent Test Criteria**: API client successfully makes authenticated requests with token injection, correctly handles various HTTP responses (success, error), and provides a consistent interface for consuming backend endpoints.
**Acceptance Criteria**:
- Scenario: Secure API Call with Token Injection
  - GIVEN an authenticated user
  - WHEN an application component requests data from `/api/conversations`
  - THEN the request automatically includes the authentication token, and data is returned successfully.

- [x] T024 [P] [US4] Define API client structure and base URL in `frontend/lib/api.ts`.
- [x] T025 [P] [US4] Implement generic `get`, `post`, `put` wrapper methods in `frontend/lib/api.ts`.
- [x] T026 [US4] Implement automatic authentication token injection into API requests from `frontend/lib/api.ts` using the active session.
- [x] T027 [US4] Implement centralized error handling for API responses in `frontend/lib/api.ts`, parsing common error structures (e.g., `ApiError` from `data-model.md`).
- [x] T028 [P] [US4] Create specific API client methods in `frontend/lib/api.ts` for backend endpoints (`/api/conversations`, `/api/messages`, `/api/poll`, `/api/health`) based on `contracts/api.yaml`.
- [x] T029 [P] [US4] Create unit tests for `frontend/lib/api.ts` covering token injection, error handling, and wrapper methods.
- [x] T030 [P] [US4] Create integration tests for API calls using `frontend/lib/api.ts` against mocked backend responses, verifying token injection and error handling.

---

## Final Phase: Polish & Cross-Cutting Concerns

**Goal**: Ensure overall quality, adherence to standards, and documentation.
**Independent Test Criteria**: N/A

- [x] T031 Update `GEMINI.md` with new technologies added during this implementation.
- [x] T032 Ensure all new components/utilities adhere to project's linting and formatting rules.
- [x] T033 Review and update `README.md` or other documentation to reflect new frontend core components.
- [x] T034 [P] Implement performance monitoring for API response times (<200ms P95) and page load (LCP <2s) and verify against thresholds in `spec.md`.
- [x] T035 [P] Conduct E2E tests to verify session persistence (99%) across browser restarts.
- [x] T036 [P] Conduct E2E/integration tests to verify 100% accurate API error handling.

---

## Summary

- **Total task count**: 36
- **Task count per user story**:
    - Setup: 5
    - Foundational (FR1): 6
    - US1 (Sign In): 4
    - US2 (Sign Out): 4
    - US3 (Session Management): 4
    - US4 (Secure API Calls): 7
    - Polish: 6
- **Parallel opportunities identified**: T011, T013, T015, T017, T019, T022, T024, T025, T028, T029, T030, T034, T035, T036. (14 tasks)
- **Independent test criteria for each story**: Defined within each User Story phase.
- **Suggested MVP scope**: User Story 1 (Sign In) and its foundational dependencies (Phase 1, Phase 2, Phase 3).

## Implementation Strategy

The feature will be implemented incrementally, prioritizing user stories based on their dependencies. Each user story phase is designed to be a shippable increment, allowing for continuous integration and testing. Development will proceed in the following order:

1.  **Phase 1: Setup**
2.  **Phase 2: Foundational (FR1: Supabase Client Utility)**
3.  **Phase 3: User Story 1 (Sign In)**
4.  **Phase 4: User Story 2 (Sign Out)**
5.  **Phase 5: User Story 3 (Session Management)**
6.  **Phase 6: User Story 4 (Secure API Calls)**
7.  **Final Phase: Polish & Cross-Cutting Concerns**
