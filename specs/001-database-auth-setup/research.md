# Phase 0: Research & Outline

This document outlines the key technical decisions made during the initial planning phase for the "Multi-Tenant Database and Authentication" feature.

## 1. Authentication Backend Strategy

- **Decision**: Use Next.js API Routes to handle all authentication-related logic (signup, signin, signout).
- **Rationale**:
  - The project's constitution specifies a Next.js frontend and a Supabase backend. The most direct and idiomatic way to connect these for authentication is via Next.js API routes using Supabase's official JavaScript client library (`@supabase/supabase-js`).
  - While the constitution also mentions a Java backend, its purpose (robust concurrency, TCP relay) is clearly oriented towards handling the high-throughput, persistent connections required for real-time chat. Using it for simple, request-response authentication flows would introduce unnecessary architectural complexity and latency.
  - This approach keeps the auth flow entirely within the Next.js/Supabase ecosystem, simplifying development and leveraging the full power of the Supabase JS library for session management.
- **Alternatives Considered**:
  - **Proxying auth through the Java backend**: Rejected due to increased complexity, latency, and deviation from standard practices for the chosen stack.

## 2. Tenant Isolation Enforcement

- **Decision**: Implement strict Row-Level Security (RLS) policies in Supabase for all tables containing tenant-specific data (`tenants`, `tenant_memberships`, `conversations`, `messages`).
- **Rationale**:
  - This directly implements Principle 2: Strict Tenant Isolation at the database layer, which is the most secure and reliable method.
  - It prevents any possibility of application-level bugs leading to cross-tenant data leaks, as the database itself enforces the security boundary.
  - Supabase is designed with RLS as a core tenet, and not using it would be a significant anti-pattern.
- **Alternatives Considered**:
  - **Application-level filtering**: Rejected as it's error-prone. A developer could forget to add a `where tenant_id = ...` clause, leading to a critical security vulnerability. RLS provides a safety net that always applies.

## 3. Clarifications Resolved

- **Initial Question**: What is the primary backend technology for request/response logic?
- **Resolution**: The Next.js web server will handle this via API Routes. The Java backend is designated for specialized real-time services.
