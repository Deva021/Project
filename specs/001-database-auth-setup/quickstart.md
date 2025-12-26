# Quickstart Guide: Database and Authentication Setup

This guide provides the steps for a developer to set up their local environment to work on the database and authentication feature.

## 1. Prerequisites

- You have been granted access to the project's Supabase instance.
- You have Node.js and npm/yarn installed.
- You have cloned the repository and are on the `001-database-auth-setup` branch.

## 2. Environment Setup

1.  **Install Dependencies**:
    Navigate to the project root and run:
    ```bash
    npm install
    ```

2.  **Configure Environment Variables**:
    Create a new file named `.env.local` in the project root. This file is ignored by git and will hold your local secrets. Add the Supabase credentials to it:

    ```env
    NEXT_PUBLIC_SUPABASE_URL=YOUR_SUPABASE_URL
    NEXT_PUBLIC_SUPABASE_ANON_KEY=YOUR_SUPABASE_ANON_KEY
    ```
    You can find these keys in your Supabase project's "API Settings".

## 3. Database Setup

The required database schema is defined in `specs/001-database-auth-setup/data-model.md`.

1.  **Run Migrations**:
    Navigate to the Supabase dashboard and open the SQL Editor.

2.  **Execute SQL**:
    Copy the entire SQL content from `data-model.md` and execute it in the SQL Editor. This will:
    - Create the `tenants`, `tenant_memberships`, `conversations`, and `messages` tables.
    - Enable Row-Level Security on these tables.
    - Create the necessary RLS policies to enforce tenant isolation.

## 4. Running the Application

Once the environment is configured and the database is migrated, you can run the Next.js development server:

```bash
npm run dev
```

The application will be available at `http://localhost:3000`. You should now be able to use the signup and signin functionality.
