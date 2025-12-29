# Data Model: Frontend UI Components

This document outlines the key data entities relevant to the Frontend UI Components, focusing on how they are represented and managed within the UI. It references core entities defined in `006-frontend-core/data-model.md`.

## 1. Conversation

Represents a chat thread between a visitor and agents. The detailed structure is defined in `../006-frontend-core/data-model.md`.

-   **Fields**: As per `006-frontend-core/data-model.md`, including `id`, `tenantId`, `title`, `status`, `createdAt`, `updatedAt`.
-   **Relationships**: Contains multiple `Message` entities. Associated with a `Tenant`.

## 2. Message

Represents an individual chat message within a conversation. The detailed structure is defined in `../006-frontend-core/data-model.md`.

-   **Fields**: As per `006-frontend-core/data-model.md`, including `id`, `conversationId`, `senderId`, `senderType`, `text`, `createdAt`.
-   **Relationships**: Belongs to a `Conversation`.

## 3. Visitor

Represents an unauthenticated user interacting with the chat widget.

-   **Name**: `Visitor`
-   **Description**: A user of a website embedding the chat widget who is not explicitly authenticated to the chat system.
-   **Fields**:
    -   `id`: `string` (UUID) - A temporary or session-based identifier for the visitor (client-generated or session-managed).
    -   `name`: `string` (optional) - Display name for the visitor.
-   **State**: Can be `active` or `inactive` based on widget interaction.
-   **Relationships**: Can initiate and send `Message` entities within a `Conversation`.

## 4. Agent

Represents an authenticated user (staff member) managing conversations via the dashboard.

-   **Name**: `Agent`
-   **Description**: An authenticated user with specific permissions to view and respond to `Conversation` entities for one or more `Tenant`s.
-   **Fields**:
    -   `id`: `string` (UUID) - User ID from Supabase Auth.
    -   `email`: `string` - Agent's email address.
    -   `tenant_memberships`: `array` - List of `Tenant` IDs the agent is authorized for.
-   **State**: `authenticated`, `online/offline` (implicitly managed).
-   **Relationships**: Can manage multiple `Conversation` entities. Can send `Message` entities. Associated with `Tenant`s via `tenant_memberships`.

## 5. Tenant

Represents a business or organization that uses the chat system.

-   **Name**: `Tenant`
-   **Description**: Provides context for conversations, agent access, and widget embedding.
-   **Fields**:
    -   `id`: `string` (UUID) - Unique identifier for the tenant.
    -   `name`: `string` - Business name (e.g., "Acme Corp").
    -   `public_key`: `string` - A public identifier used by the widget to associate with a tenant.
-   **Relationships**: Owns multiple `Conversation` entities. Associated with `Agent` entities.
