# Quickstart Guide: Frontend Core Components

This guide provides a quick overview for developers on how to integrate and utilize the new Frontend Core Components.

## 1. Supabase Client Integration

The `lib/supabase.ts` utility exports a `createClient` function for direct Supabase client access and includes essential authentication helpers.

### Initializing and Using Auth Helpers

```typescript
// Import in client-side components or pages
import { signIn, signOut, getSession } from '../lib/supabase';

// Example: Sign in a user
async function handleLogin(email, password) {
  try {
    await signIn(email, password);
    console.log('User signed in successfully!');
    // Redirect or update UI
  } catch (error) {
    console.error('Login failed:', error.message);
  }
}

// Example: Get current session
async function getCurrentUserSession() {
  const session = await getSession();
  if (session) {
    console.log('Current user:', session.user);
  } else {
    console.log('No active session.');
  }
}

// Example: Sign out a user
async function handleLogout() {
  try {
    await signOut();
    console.log('User signed out successfully!');
    // Redirect to login or update UI
  } catch (error) {
    console.error('Logout failed:', error.message);
  }
}
```

## 2. API Client Usage

The `lib/api.ts` client simplifies interaction with the backend REST API, automatically handling authentication token injection and error handling.

### Making Authenticated API Calls

```typescript
// Import the API client
import apiClient from '../lib/api'; // Assuming lib/api.ts exports a default instance

// Example: Fetch conversations for a tenant
async function fetchConversations(tenantId: string) {
  try {
    const data = await apiClient.get(`/conversations?tenantId=${tenantId}`);
    console.log('Conversations:', data);
  } catch (error) {
    console.error('Failed to fetch conversations:', error.message);
  }
}

// Example: Create a new conversation
async function createNewConversation(tenantId: string, title: string, initialMessage: string) {
  try {
    const data = await apiClient.post('/conversations', { tenant_id: tenantId, title, message: initialMessage });
    console.log('New conversation:', data);
  } catch (error) {
    console.error('Failed to create conversation:', error.message);
  }
}

// Example: Send a message
async function postMessage(conversationId: string, text: string, senderType: 'visitor' | 'agent') {
  try {
    const data = await apiClient.post(`/conversations/${conversationId}/messages`, { text, sender_type: senderType });
    console.log('Message sent:', data);
  } catch (error) {
    console.error('Failed to send message:', error.message);
  }
}
```

## 3. Auth Context Integration

The `AuthContext` provides global access to authentication state and actions within your React components.

### Consuming Auth Context

```typescript
// AuthContext.tsx (Example structure)
// import React, { createContext, useContext, useState, useEffect } from 'react';
// import { getSession, signIn, signOut } from '../lib/supabase';
// import { Session, User } from '@supabase/supabase-js';

// type AuthContextType = {
//   session: Session | null;
//   user: User | null;
//   isLoading: boolean;
//   login: (email: string, password: string) => Promise<void>;
//   logout: () => Promise<void>;
// };

// const AuthContext = createContext<AuthContextType | undefined>(undefined);

// export const AuthProvider = ({ children }) => {
//   const [session, setSession] = useState<Session | null>(null);
//   const [user, setUser] = useState<User | null>(null);
//   const [isLoading, setIsLoading] = useState(true);

//   useEffect(() => {
//     const fetchSession = async () => {
//       const currentSession = await getSession();
//       setSession(currentSession);
//       setUser(currentSession?.user || null);
//       setIsLoading(false);
//     };
//     fetchSession();
//   }, []);

//   const login = async (email, password) => {
//     setIsLoading(true);
//     try {
//       await signIn(email, password);
//       const currentSession = await getSession();
//       setSession(currentSession);
//       setUser(currentSession?.user || null);
//     } finally {
//       setIsLoading(false);
//     }
//   };

//   const logout = async () => {
//     setIsLoading(true);
//     try {
//       await signOut();
//       setSession(null);
//       setUser(null);
//     } finally {
//       setIsLoading(false);
//     }
//   };

//   return (
//     <AuthContext.Provider value={{ session, user, isLoading, login, logout }}>
//       {children}
//     </AuthContext.Provider>
//   );
// };

// export const useAuth = () => {
//   const context = useContext(AuthContext);
//   if (context === undefined) {
//     throw new Error('useAuth must be used within an AuthProvider');
//   }
//   return context;
// };


// Example: Using the Auth Context in a component
import { useAuth } from '../components/AuthContext'; // Assuming AuthContext is in components/AuthContext.tsx

function MyComponent() {
  const { user, isLoading, logout } = useAuth();

  if (isLoading) {
    return <div>Loading authentication state...</div>;
  }

  if (user) {
    return (
      <div>
        <p>Welcome, {user.email}!</p>
        <button onClick={logout}>Logout</button>
      </div>
    );
  }

  return <div>Please log in.</div>;
}
```
