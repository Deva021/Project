<!--
Sync Impact Report:

* Version change: 0.0.0 → 1.0.0
* Added sections:
  * Project Name
  * Principle 1: Server Components by Default
  * Principle 2: Strict Tenant Isolation
  * Principle 3: Robust Concurrency in Java Backend
  * Principle 4: High-Performance User Experience
  * Principle 5: Specification-First Development
  * Governance
* Removed sections: None
* Templates requiring updates:
  * ⚠ pending: `.specify/templates/plan-template.md`
  * ⚠ pending: `.specify/templates/spec-template.md`
  * ⚠ pending: `.specify/templates/tasks-template.md`
  * ⚠ pending: `.specify/templates/commands/speckit.constitution.md`
-->

# Constitution for [PROJECT_NAME]

| Version   | Ratification Date | Last Amended Date |
| :-------- | :---------------- | :---------------- |
| **1.0.0** | 2025-12-25        | 2025-12-25        |

## Project Name

Multi-tenant live chat

## Principles

### 1. Principle: Server Components by Default

**Rule:** All UI components in the Next.js application MUST be Server Components by default. The `'use client'` directive should only be used as a last resort for components requiring client-side interactivity (e.g., event handlers, state, lifecycle effects).

**Rationale:** Maximizes server-side rendering benefits, reduces the client-side JavaScript bundle size, improves initial page load performance, and keeps sensitive logic and data access on the server.

**Example: Correct Pattern**

```javascript
// app/chat/page.tsx (Server Component)
import { getMessages } from '../lib/data';
import Messages from './messages';

// This is a Server Component by default. It can directly access server-side resources.
export default async function ChatPage({ params }: { params: { tenantId: string } }) {
  const messages = await getMessages(params.tenantId); // Fetches data on the server

  return (
    <div>
      <h1>Live Chat</h1>
      <Messages initialMessages={messages} />
    </div>
  );
}

// app/chat/messages.tsx (Client Component)
'use client';

import { useState, useEffect } from 'react';
import { supabase } from '../lib/supabaseClient';

// This component needs state and effects, so it must be a Client Component.
export default function Messages({ initialMessages }) {
  const [messages, setMessages] = useState(initialMessages);

  useEffect(() => {
    const channel = supabase.channel('realtime-chat')...
    // ...real-time subscription logic
    return () => supabase.removeChannel(channel);
  }, []);

  return (
    <ul>
      {messages.map(msg => <li key={msg.id}>{msg.content}</li>)}
    </ul>
  );
}
```

### 2. Principle: Strict Tenant Isolation

**Rule:** All data access, especially database queries via Supabase, MUST be strictly and automatically scoped to the authenticated user's `tenant_id`. Direct queries that omit a `WHERE tenant_id = ?` clause are forbidden. Row-Level Security (RLS) MUST be enabled and enforced in Supabase for all tables containing tenant data.

**Rationale:** Prevents data leakage between tenants, which is the most critical security requirement for a multi-tenant application.

**Example: Tenant Isolation Enforcement**

```sql
-- Supabase RLS Policy
CREATE POLICY "Enable read access for user's own tenant"
ON public.messages
FOR SELECT
USING (
  auth.uid() IN (
    SELECT user_id FROM tenants_users WHERE tenant_id = messages.tenant_id
  )
);
```

```javascript
// lib/data.ts (Supabase Query)
import { createServerClient } from '@supabase/ssr';

export async function getMessages(tenantId) {
  const supabase = createServerClient(...);
  const { data: sessionData, error: sessionError } = await supabase.auth.getSession();

  if (sessionError || !sessionData.session) {
    throw new Error("Authentication required.");
  }

  // RLS in Supabase handles the tenant_id check automatically based on the user's session.
  // This query will only ever return messages for the tenants the user belongs to.
  const { data, error } = await supabase
    .from('messages')
    .select('*')
    .eq('tenant_id', tenantId); // Explicit check still good practice for clarity

  if (error) throw error;
  return data;
}
```

### 3. Principle: Robust Concurrency in Java Backend

**Rule:** The Java backend, including Servlets and the TCP Relay, MUST use thread-safe patterns and data structures for managing shared resources, connections, and state. This includes using `java.util.concurrent` classes like `BlockingQueue` for message passing, `ExecutorService` for managing thread pools, and `ConcurrentHashMap` for shared caches or connection lists.

**Rationale:** Ensures application stability, prevents race conditions, and guarantees reliable message delivery and state management under high load with multiple simultaneous client connections.

**Example: Concurrency Best Practices**

```java
// MessageRelayServlet.java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageRelayServlet extends HttpServlet {
    private ExecutorService messageProcessor;
    private BlockingQueue<String> inboundQueue;

    @Override
    public void init() {
        messageProcessor = Executors.newFixedThreadPool(10);
        inboundQueue = new LinkedBlockingQueue<>();
        // Start a background worker to process messages from the queue
        messageProcessor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String message = inboundQueue.take();
                    // Process and relay message to TCP clients
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String message = req.getReader().lines().collect(Collectors.joining());
        // Safely offload message processing to another thread
        boolean offered = inboundQueue.offer(message);
        if (!offered) {
            // Handle backpressure
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is busy.");
        }
    }
    // ... destroy() method to shut down ExecutorService
}
```

### 4. Principle: High-Performance User Experience

**Rule:** The application MUST adhere to the following performance budgets:

- **API Response Time:** P95 for all critical APIs must be < 200ms.
- **Page Load Time:** Largest Contentful Paint (LCP) for key pages must be < 2.0 seconds.
- **Bundle Size:** The initial client-side JavaScript bundle for any given page must not exceed 300KB.

**Rationale:** A fast, responsive user experience is critical for user engagement and retention in a real-time chat application. These metrics provide clear, measurable targets for development and quality assurance.

### 5. Principle: Specification-First Development

**Rule:** All new features or significant architectural changes MUST begin with a formal specification document created via the `speckit.specify` agent. The specification must be reviewed and approved before any implementation work begins.

**Rationale:** Ensures that business requirements, technical approach, and potential impacts are fully understood and agreed upon upfront. This reduces wasted effort, improves alignment between product and engineering, and creates a historical record of technical decisions.

## Governance

### Amendment Process

Amendments to this constitution require a formal proposal and review. The `speckit.constitution` agent must be used to draft the changes, which will then be committed to the repository for team review.

### Versioning

This constitution follows Semantic Versioning 2.0.0:

- **MAJOR** version change for backward-incompatible changes (e.g., removing a principle).
- **MINOR** version change for adding new principles or features that are backward-compatible.
- **PATCH** version change for minor clarifications, typo fixes, or edits that do not change the substance of a rule.

### Compliance

All code and specifications MUST adhere to the principles outlined in this document. Automated checks, linters, and code review processes will be configured to enforce these rules where possible.
