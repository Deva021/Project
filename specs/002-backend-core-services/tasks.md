# Tasks for Backend Core Services

## Implementation Strategy

The implementation will be phased to ensure foundational components are built first, followed by user-story-driven feature development. Each user story phase is designed to be an independently testable increment of functionality.

- **MVP Scope**: The MVP for this feature includes the completion of Phase 1, 2 and 3. This will deliver a functional authentication and authorization filter.
- **Incremental Delivery**: Phase 4 builds upon the MVP to add the critical tenant-aware data access logic. The final phase adds robustness through tests and documentation.

---

## Phase 1: Setup

These tasks prepare the project environment and dependencies.

- [ ] T001 Add Maven dependencies for PostgreSQL, HikariCP, and java-jwt to `backend/pom.xml`.

---

## Phase 2: Foundational Components

This phase creates the core, non-functional shells for the services. They are prerequisites for all user stories.

- [ ] T002 Create the `TenantContext` class for `ThreadLocal` storage in `backend/src/main/java/com/minintercom/common/TenantContext.java`.
- [ ] T003 [P] Implement the basic `DatabaseService` class with HikariCP setup in `backend/src/main/java/com/minintercom/common/DatabaseService.java`.
- [ ] T004 [P] Implement the basic `JwtService` class structure in `backend/src/main/java/com/minintercom/security/JwtService.java`.

---

## Phase 3: User Story 1 & 2 - Authentication & Authorization Filter

- **Story Goal**: Intercept incoming requests, validate JWTs to grant or deny access, and establish tenant context for valid requests.
- **Independent Test Criteria**: When the `AuthFilter` is active, requests to protected endpoints with a valid JWT succeed, while requests with an invalid or missing JWT are rejected with a 401 status.

### Implementation Tasks

- [ ] T005 [US1] Create the `AuthFilter` class implementing `javax.servlet.Filter` in `backend/src/main/java/com/minintercom/security/AuthFilter.java`.
- [ ] T006 [US1] Implement token validation logic in `JwtService` using `com.auth0:java-jwt` at `backend/src/main/java/com/minintercom/security/JwtService.java`.
- [ ] T007 [US1] In `AuthFilter`, use `JwtService` to validate the `Authorization` header.
- [ ] T008 [US1] If token is valid, extract the `tenant_id` claim and set it in `TenantContext`.
- [ ] T009 [US2] If token is invalid or missing, reject the request with an HTTP 401 Unauthorized error in `AuthFilter`.
- [ ] T010 [US1] Ensure the `AuthFilter` clears the `TenantContext` in a `finally` block to prevent context leakage.
- [ ] T011 [US1] Register the `AuthFilter` in `backend/src/main/webapp/WEB-INF/web.xml` to protect the `/api/*` URL pattern.

---

## Phase 4: User Story 3 - Tenant-Aware Data Access

- **Story Goal**: Ensure all data access through `DatabaseService` is automatically and securely scoped to the current tenant.
- **Independent Test Criteria**: A test making two parallel requests with different valid tenant JWTs must demonstrate that each request can only retrieve data belonging to its own tenant.

### Implementation Tasks

- [ ] T012 [US3] Modify `DatabaseService` to read the tenant ID from `TenantContext` in `backend/src/main/java/com/minintercom/common/DatabaseService.java`.
- [ ] T013 [US3] Implement a tenant-aware data access method (e.g., `findUserById`) in `DatabaseService` that automatically injects the tenant ID into the SQL query's WHERE clause.

---

## Final Phase: Polish & Testing

- **Goal**: Harden the feature with documentation and automated tests.

### Implementation Tasks

- [ ] T014 [P] Add comprehensive Javadoc comments to all new public classes and methods.
- [ ] T015 [P] Write unit tests for `JwtService` covering valid, invalid, and expired tokens in `backend/src/test/java/com/minintercom/security/JwtServiceTest.java`.
- [ ] T016 [P] Write unit tests for `DatabaseService` using mocks to verify that the tenant ID is correctly applied to SQL queries in `backend/src/test/java/com/minintercom/common/DatabaseServiceTest.java`.
- [ ] T017 Write an integration test for the `AuthFilter` pipeline in `backend/src/test/java/com/minintercom/security/AuthFilterIT.java`.

---

## Dependencies

- **Phase 3** depends on **Phase 2**.
- **Phase 4** depends on **Phase 3**.
- **Final Phase** can be worked on in parallel but is dependent on the completion of the respective components in Phases 3 and 4.

## Parallel Execution

- Within Phase 2, `T003` and `T004` can be done in parallel.
- Within the Final Phase, `T014`, `T015`, and `T016` can be done in parallel once their corresponding components are complete.
- The user story phases (3 and 4) are sequential and must be completed in order.
