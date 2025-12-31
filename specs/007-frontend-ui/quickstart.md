# Quickstart Guide: Frontend UI Components

This guide provides a quick overview for developers on how to integrate and utilize the new Frontend UI Components.

## 1. Widget UI Integration

The Chat Widget is designed to be embedded into any website. It functions in visitor mode, allowing unauthenticated users to start and participate in conversations.

### How to use `app/widget/page.tsx` and `ChatWindow.tsx`

The `app/widget/page.tsx` will serve as the entry point for the widget, typically loaded within an iframe. It will instantiate and manage the `ChatWindow.tsx` component.

```typescript
// Example of app/widget/page.tsx structure (simplified)
'use client';

import { useState } from 'react';
import ChatWindow from '../../components/ChatWindow'; // Assuming ChatWindow is a Client Component

export default function WidgetPage() {
  const [isOpen, setIsOpen] = useState(false); // Example: widget open/close state

  // Logic to determine tenantId from URL parameters or host origin

  return (
    <div className="widget-container">
      {/* Button to open/close widget */}
      <button onClick={() => setIsOpen(!isOpen)}>
        {isOpen ? 'Close Chat' : 'Open Chat'}
      </button>

      {isOpen && (
        <ChatWindow tenantId="some-tenant-id" conversationId="optional-conv-id" />
      )}
    </div>
  );
}
```

## 2. Dashboard UI Usage

The Agent Dashboard provides an interface for authenticated agents to manage conversations.

### How to use `app/dashboard/page.tsx` and `ConversationList.tsx`

The `app/dashboard/page.tsx` (which will be enhanced from the existing placeholder) will orchestrate the display of conversations using `ConversationList.tsx` and a detailed view of the selected conversation.

```typescript
// Example of app/dashboard/page.tsx structure (simplified)
'use client';

import { useState } from 'react';
import { useAuth } from '../../components/AuthContext';
import ConversationList from '../../components/ConversationList'; // Assuming ConversationList is a Client Component
// import ConversationView from '../../components/ConversationView'; // A component for selected conversation view

export default function DashboardPage() {
  const { user, isLoading } = useAuth();
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);

  if (isLoading || !user) {
    return <div>Loading or not authenticated...</div>; // AuthContext handles redirection
  }

  return (
    <div className="dashboard-layout">
      <aside className="conversation-sidebar">
        <ConversationList agentId={user.id} onSelectConversation={setSelectedConversationId} />
      </aside>
      <main className="main-content">
        {selectedConversationId ? (
          // <ConversationView conversationId={selectedConversationId} agentId={user.id} />
          <div>Selected Conversation: {selectedConversationId}</div>
        ) : (
          <div>Select a conversation</div>
        )}
      </main>
    </div>
  );
}
```

## 3. Demo Page Implementation

The `app/page.tsx` will serve as the system's landing and demo page.

### How to implement `app/page.tsx`

This page will primarily be a Server Component to render static content, explanations, and embed code examples. It can dynamically render multiple widget instances or links to widget pages for different tenants.

```typescript
// Example of app/page.tsx structure (simplified)
import Link from 'next/link';

export default function HomePage() {
  return (
    <div>
      <h1>Welcome to Mini Intercom Demo!</h1>
      <p>This page demonstrates our multi-tenant live chat system.</p>

      <h2>Embed Widget Example</h2>
      <pre>
        {`<iframe src="https://your-app.com/widget?tenantId=tenant-abc" width="300" height="400"></iframe>`}
      </pre>

      <h2>Tenant-Specific Demos</h2>
      <Link href="/widget?tenantId=tenant-1">Demo for Tenant 1</Link>
      <Link href="/widget?tenantId=tenant-2">Demo for Tenant 2</Link>

      {/* Other content, system architecture, etc. */}
    </div>
  );
}
```
