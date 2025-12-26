# Tasks: Multi-Tenant Database and Authentication

- **Feature:** Database Schema Setup
- **Type:** `task`
- **Principle:** `P2-Tenant`
- **Description:** Apply the SQL schema from `data-model.md` to the Supabase instance, creating tables for tenants, memberships, conversations, and messages.
- **Acceptance Criteria:**

  - [x] `tenants` table created with correct columns
  - [x] `tenant_memberships` table created with correct columns and FKs
  - [x] `conversations` table created with correct columns and FKs
  - [x] `messages` table created with correct columns and FKs
  - [x] RLS enabled on all 4 tables

- **Feature:** RLS Policy Implementation
- **Type:** `task`
- **Principle:** `P2-Tenant`
- **Description:** Implement the `is_member_of_tenant` helper function and apply Row-Level Security policies to enforce strict tenant isolation.
- **Acceptance Criteria:**

  - [x] `is_member_of_tenant` function exists and works
  - [x] Policy: Users can only view their own tenants
  - [x] Policy: Users can only view memberships for their tenants
  - [x] Policy: Users can only access conversations/messages for their tenants
  - [x] Manual verification confirms cross-tenant access is blocked

- **Feature:** Backend Auth API
- **Type:** `task`
- **Principle:** `P1-Server`
- **Description:** Create Next.js API routes for `signup`, `signin`, and `signout` using the Supabase client.
- **Acceptance Criteria:**

  - [ ] `POST /api/auth/signup` creates a user and tenant
  - [ ] `POST /api/auth/signin` returns a valid session
  - [ ] `POST /api/auth/signout` clears the session
  - [ ] API handles errors (invalid credentials, duplicate email) gracefully

- **Feature:** Frontend UI Components
- **Type:** `task`
- **Principle:** `P1-Server`
- **Description:** Build the Signup and Sign-in pages using Server Components for layout and Client Components for interactive forms.
- **Acceptance Criteria:**

  - [ ] `/login` page renders with email/password form
  - [ ] `/signup` page renders with email/password/tenant-name form
  - [ ] Forms submit to the backend API
  - [ ] Loading states and error messages are displayed

- **Feature:** Session Management
- **Type:** `task`
- **Principle:** `P2-Tenant`
- **Description:** Implement client-side session context and middleware to protect routes.
- **Acceptance Criteria:**

  - [ ] Middleware redirects unauthenticated users from protected routes (e.g., `/dashboard`)
  - [ ] Auth state is accessible via React Context or Hook
  - [ ] Session persists across page reloads

- **Feature:** Integration Testing
- **Type:** `task`
- **Principle:** `P5-Spec`
- **Description:** Verify the end-to-end authentication flow and tenant isolation security.
- **Acceptance Criteria:**
  - [ ] Full flow: Signup -> Dashboard -> Logout -> Login works
  - [ ] Verified: User A cannot see User B's tenant data
