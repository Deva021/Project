# Data Model for Backend Core Services

This document outlines the conceptual data model for the entities managed by the core services. The physical schema will be defined and managed via Supabase migrations.

## Core Entities

### User
Represents an individual user of the application.

- **id**: `UUID` (Primary Key)
- **email**: `String` (Unique)
- **created_at**: `Timestamp`

### Tenant
Represents a customer or organization, providing a scope for data isolation.

- **id**: `UUID` (Primary Key)
- **name**: `String`
- **created_at**: `Timestamp`

### TenantUser (Join Table)
Links users to the tenants they belong to, establishing a many-to-many relationship. A user can be part of multiple tenants, and a tenant can have multiple users.

- **tenant_id**: `UUID` (Foreign Key to Tenant.id)
- **user_id**: `UUID` (Foreign Key to User.id)
- **role**: `String` (e.g., 'admin', 'member')

## Relationships

- A `User` can be a member of one or more `Tenants`.
- A `Tenant` can have one or more `Users`.
- The `TenantUser` table defines this many-to-many relationship and assigns a role to the user within that specific tenant.
