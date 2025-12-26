# Backend Core Services - Implementation Plan

## 1. Objective

Implement a set of core backend services for database access, JWT-based authentication, and authorization, as detailed in the feature specification. These services will form the foundation for secure and efficient backend operations.

## 2. Technical Context
- **Backend Stack**: Java, using Servlets.
- **Database**: Supabase (PostgreSQL), accessed via JDBC.
- **Authentication**: JWT-based.
- **Key Components**:
  - `DatabaseService`: Manages database connections via HikariCP connection pool and provides CRUD helper methods.
  - `JwtService`: Validates JWTs, extracts claims, and resolves user identity.
  - `AuthFilter`: A Servlet `Filter` to intercept requests, validate authentication, and enforce tenant-based authorization.
- **Dependencies**:
  - `org.postgresql:postgresql`
  - `com.zaxxer:HikariCP`
  - `javax.servlet:javax.servlet-api`
  - `com.auth0:java-jwt` (Decision documented in `research.md`)
- **Project Structure**: All components will be built within the `backend` Maven project.

## 3. Constitution Checklist

- [ ] **Principle 1: Server Components by Default:** [N/A] This is a backend-only feature.
- [x] **Principle 2: Strict Tenant Isolation:** The tenant isolation strategy has been defined in `research.md`. It uses a `ServletFilter` to set a `ThreadLocal` tenant context, which the `DatabaseService` will use to scope all queries. This resolves the initial violation.
- [x] **Principle 3: Robust Concurrency:** The use of HikariCP and thread-safe service design aligns with this principle.
- [x] **Principle 4: High-Performance UX:** The use of a connection pool and efficient in-memory JWT validation is critical for meeting the <200ms API response time budget.
- [x] **Principle 5: Specification-First:** This plan is based on the approved specification `002-backend-core-services/spec.md`.

## 4. Implementation Steps

| Step | Description | Owner | Status |
| :--- | :---------- | :---- | :----- |
| 1.   | Phase 0: Research and select a JWT library. | TBD | Done   |
| 2.   | Add Maven dependencies for all required libraries. | TBD | To Do  |
| 3.   | Implement `TenantContext` and `DatabaseService` using the `ThreadLocal` pattern from `research.md`. | TBD | To Do  |
| 4.   | Design and implement `JwtService` using `com.auth0:java-jwt`. | TBD | To Do  |
| 5.   | Design and implement `AuthFilter` for authentication & authorization. | TBD | To Do  |
| 6.   | Write unit and integration tests for all new services. | TBD | To Do  |

## 5. Testing Strategy

- **Unit Tests**: Each service (`DatabaseService`, `JwtService`) will have dedicated unit tests using mocks (e.g., Mockito) to isolate its logic.
- **Integration Tests**: A suite of integration tests will validate the filter and service pipeline. This will use an embedded servlet container (like Jetty) and a test database (e.g., Testcontainers) to simulate real-world request processing.
- **E2E Tests**: Not directly applicable for this feature, but will be essential for features that consume these services.