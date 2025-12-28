# Data Model: Frontend Core Components

This document outlines the key data entities relevant to the Frontend Core Components, focusing on how they are represented and managed within the frontend application. These entities primarily reflect data received from Supabase Auth and the backend API.

## 1. User

Represents an authenticated user within the application context. This data is primarily sourced from Supabase Auth.

-   **Name**: `User`
-   **Description**: Details of the currently authenticated user.
-   **Fields**:
    -   `id`: `string` (UUID) - Unique identifier for the user. (From Supabase `auth.user().id`)
    -   `email`: `string` - User's email address. (From Supabase `auth.user().email`)
    -   `user_metadata`: `object` - Additional metadata associated with the user (e.g., `display_name`).
-   **Relationships**: Has one `Session`.

## 2. Session

Represents the active authentication session for a user. This data is also managed by Supabase Auth.

-   **Name**: `Session`
-   **Description**: Contains authentication tokens and session details for an authenticated user.
-   **Fields**:
    -   `access_token`: `string` - JWT used for authenticating requests to Supabase and the backend API.
    -   `refresh_token`: `string` - Token used to obtain new access tokens without re-authenticating.
    -   `expires_in`: `number` - Duration (in seconds) until the access token expires.
    -   `expires_at`: `number` - Timestamp (in seconds) when the access token expires.
    -   `user`: `User` - The associated User entity.
-   **State Transitions**: Sessions are established upon successful login, refreshed periodically by Supabase, and invalidated upon logout or expiry.

## 3. API Response

A standardized structure expected for responses from the backend API. This ensures consistent error handling and data parsing.

-   **Name**: `ApiResponse<T>`
-   **Description**: Generic structure for data returned from backend API calls.
-   **Fields**:
    -   `data`: `T` - The actual data payload if the request was successful. The type `T` varies based on the endpoint (e.g., `Conversation[]`, `Message[]`).
    -   `error`: `ApiError | null` - An error object if the request failed, otherwise `null`.
-   **Related Entities**: `ApiError`

## 4. ApiError

A standardized error object returned by the backend API in case of a failed request.

-   **Name**: `ApiError`
-   **Description**: Details about an error encountered during an API request.
-   **Fields**:
    -   `code`: `string` - An application-specific error code.
    -   `message`: `string` - A human-readable error message.
    -   `status`: `number` - The HTTP status code of the error response.
    -   `details`: `object | null` - Optional, additional details about the error.
