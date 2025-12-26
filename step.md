# 📦 Modular Specification Structure (Puzzle Pieces)

## Foundation Layer (Build First)

1. **Database Schema Specification**

   - Tables: tenants, tenant_memberships, conversations, messages
   - Relationships and constraints
   - Sample data
   - **Output:** Working database ready for queries

2. **Authentication & Authorization Specification**
   - Supabase Auth setup
   - JWT verification flow
   - Tenant membership validation
   - **Output:** Auth system ready to protect endpoints

## Backend Core (Build Second)

3. **Database Service Specification**

   - Connection pooling (Hikari)
   - CRUD operations per table
   - Transaction management
   - **Output:** `Db.java` + helper classes

4. **JWT Service Specification**

   - Token verification
   - Claims extraction
   - User identity resolution
   - **Output:** `JwtService.java`

5. **Auth Filter Specification**
   - Request interception
   - Token validation
   - Tenant authorization
   - **Output:** `AuthFilter.java`

## Business Logic (Build Third)

6. **Conversation Service Specification**

   - Create conversation (public)
   - List conversations per tenant (protected)
   - Conversation status management
   - **Output:** `ConversationService.java`

7. **Message Service Specification**
   - Send visitor message (public)
   - Send agent reply (protected)
   - Fetch message history
   - Tenant-scoped queries
   - **Output:** `MessageService.java`

## Real-Time Layer (Build Fourth)

8. **Relay Server Protocol Specification**

   - TCP message format
   - Connect/disconnect handling
   - Event types (new_message, agent_typing, etc.)
   - **Output:** Protocol design document

9. **Relay Server Implementation Specification**

   - ServerSocket setup
   - Thread pool configuration
   - Client handler per connection
   - **Output:** `RelayServer.java`, `ClientHandler.java`

10. **Relay Router Specification**

    - Event routing by tenant/conversation
    - Subscriber management
    - Broadcast logic
    - **Output:** `Router.java`

11. **Relay Client Specification**

    - TCP client from backend
    - Publish events to relay
    - Connection retry logic
    - **Output:** `RelayClient.java`

12. **Poll Queue Service Specification**
    - BlockingQueue per tenant/conversation
    - Long-poll timeout handling
    - Event notification
    - **Output:** `PollQueueService.java`

## REST API (Build Fifth)

13. **Health Servlet Specification**

    - Simple health check endpoint
    - **Output:** `HealthServlet.java`

14. **Conversations Servlet Specification**

    - POST: Create conversation (public)
    - GET: List conversations (protected)
    - **Output:** `ConversationsServlet.java`

15. **Messages Servlet Specification**

    - POST: Send message (public + protected)
    - GET: Fetch history
    - **Output:** `MessagesServlet.java`

16. **Poll Servlet Specification**
    - GET: Long-poll endpoint
    - Queue integration
    - Timeout handling
    - **Output:** `PollServlet.java`

## Frontend Core (Build Sixth)

17. **Supabase Client Specification**

    - Client initialization
    - Auth helpers
    - **Output:** `lib/supabase.ts`

18. **API Client Specification**

    - Fetch wrappers for all endpoints
    - Token injection
    - Error handling
    - **Output:** `lib/api.ts`

19. **Auth Context Specification**
    - Login/logout
    - Session management
    - User state
    - **Output:** `AuthContext.tsx` or `AuthGate.tsx`

## Frontend UI (Build Seventh)

20. **Widget UI Specification**

    - Chat window component
    - Message composer
    - Visitor mode (no auth)
    - **Output:** `app/widget/page.tsx`, `ChatWindow.tsx`

21. **Dashboard UI Specification**

    - Conversation list
    - Selected conversation view
    - Agent reply interface
    - **Output:** `app/dashboard/page.tsx`, `ConversationList.tsx`

22. **Demo Page Specification**
    - Landing page
    - Embed code examples
    - Multiple tenant demo
    - **Output:** `app/page.tsx`

## Integration (Build Eighth)

23. **Widget Embedding Specification**

    - iframe injection script
    - Cross-origin configuration
    - **Output:** `widget-loader.js`

24. **Real-Time Integration Specification**
    - Connect poll endpoint to UI
    - Auto-refresh on events
    - Loading states
    - **Output:** Integrated frontend + backend

## Testing & Polish (Build Last)

25. **Multi-Tenant Testing Specification**

    - Test data isolation
    - Multiple simultaneous conversations
    - Cross-tenant security tests
    - **Output:** Test plan + results

26. **End-to-End Flow Specification**
    - Visitor journey
    - Agent journey
    - Demo script
    - **Output:** Working demo
