# 💬 Mini Intercom — Multi-Tenant Live Support Chat

An Intercom-style **"Chat with us"** system that can be embedded into any website. One deployment supports **multiple businesses (tenants)** with isolated data, an **agent dashboard**, and a **visitor widget**.

**Tech Stack:** Next.js (TypeScript) • Java Servlets (Tomcat) • Java TCP Relay Server (Sockets + Threads) • Supabase (Postgres + Auth/JWT)

---

## 📋 Table of Contents

- [What We're Building](#-what-were-building)
- [Why This Fits Advanced Programming](#-why-this-fits-advanced-programming)
- [System Architecture](#-system-architecture)
- [Multi-Tenant Message Routing](#-multi-tenant-message-routing-how-it-works)
- [Project Structure](#%EF%B8%8F-project-structure)
- [Database Structure](#%EF%B8%8F-database-structure)
- [Authentication & Authorization](#-authentication--authorization)
- [Real-Time Updates Design](#-real-time-updates-design)
- [API Overview](#-api-overview)
- [Local Setup](#-local-setup-guide)
- [Milestones](#-milestones)
- [Demo Script](#-demo-and-presentation-script)

---

## ✅ What We're Building

### 👤 Visitor Experience (Widget)

- **Embedded widget** loaded via iframe (no CSS conflicts with host site)
- Start conversations and send messages
- Receive agent replies with **near real-time updates**
- Widget identifies the business automatically

### 🎧 Agent Experience (Dashboard)

- **Secure login** using Supabase Auth (JWT)
- View conversation inbox for **their business only**
- Reply to visitors in real-time
- Live updates without page refresh (long-poll + event queue)

### 🏢 Multi-Tenancy (Multiple Businesses)

- **One deployment serves many businesses** (restaurants, stores, etc.)
- **Complete data isolation** - each business sees only their data
- Widget uses `tenant_public_key` (safe to expose on websites)
- Agents authorized per tenant via `tenant_memberships`

---

## 🔥 Why This Fits "Advanced Programming"

This project demonstrates the three core course requirements:

| Requirement  | Implementation                                               |
| ------------ | ------------------------------------------------------------ |
| **Servlets** | REST endpoints + JWT auth filter + long-poll endpoint        |
| **Sockets**  | TCP relay server broadcasting message events                 |
| **Threads**  | ExecutorService + BlockingQueue + concurrent message routing |

---

## 🧩 System Architecture

### High-Level Components

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│   Next.js App   │      │  Java Servlets   │      │   TCP Relay     │
│                 │      │    (Tomcat)      │      │     Server      │
│ • Demo Page     │◄────►│                  │◄────►│                 │
│ • Widget        │      │ • Auth Filter    │      │ • Sockets       │
│ • Dashboard     │      │ • REST API       │      │ • Threads       │
│                 │      │ • Long-poll      │      │ • Broadcast     │
└─────────────────┘      └──────────────────┘      └─────────────────┘
                                 │
                                 ▼
                         ┌──────────────────┐
                         │    Supabase      │
                         │  Postgres + Auth │
                         └──────────────────┘
```

### Message Flow (Visitor → Agent)

```
1. Visitor on website → Opens widget (iframe)
2. Widget identifies business via tenant_public_key
3. Visitor sends message → Servlet API
4. Servlet API → Saves to Supabase Postgres (scoped by tenant_id)
5. Servlet API → Publishes event to TCP Relay Server
6. Relay → Broadcasts to subscribed agents (filtered by tenant_id)
7. Agent dashboard long-poll → Releases immediately
8. Dashboard → Fetches and displays new message
```

**Agent Reply (Agent → Visitor):** Same pipeline in reverse.

---

## 🎯 Multi-Tenant Message Routing (How It Works)

### The Problem

Different websites (restaurants, stores, supermarkets) need to use the same chat system, but:

- Restaurant agents should only see restaurant messages
- Store agents should only see store messages
- Complete data isolation between businesses

### The Solution: Tenant-based Routing

#### 1️⃣ Setup Phase (One-time per business)

**Each business gets a unique tenant record:**

| Business         | tenant_id | public_key     | Name          |
| ---------------- | --------- | -------------- | ------------- |
| Pizza Restaurant | 1         | `rest_abc123`  | "Joe's Pizza" |
| City Supermarket | 2         | `super_xyz456` | "Fresh Mart"  |
| Tech Store       | 3         | `store_def789` | "Gadget Hub"  |

**Each business embeds their unique widget:**

```html
<!-- On Pizza Restaurant's website -->
<iframe src="https://your-app.com/widget?key=rest_abc123"></iframe>

<!-- On Supermarket's website -->
<iframe src="https://your-app.com/widget?key=super_xyz456"></iframe>

<!-- On Tech Store's website -->
<iframe src="https://your-app.com/widget?key=store_def789"></iframe>
```

**Agents are assigned to specific tenants:**

| Agent | tenant_id | Role                          |
| ----- | --------- | ----------------------------- |
| John  | 1         | agent (Pizza Restaurant only) |
| Sarah | 2         | agent (Supermarket only)      |
| Mike  | 3         | agent (Tech Store only)       |

#### 2️⃣ Runtime: Message Routing in Action

**Example: 3 visitors asking questions simultaneously**

| Website          | Visitor Message               | public_key Used | Resolved tenant_id | Agent Who Sees It |
| ---------------- | ----------------------------- | --------------- | ------------------ | ----------------- |
| Pizza Restaurant | "Do you deliver to downtown?" | `rest_abc123`   | 1                  | ✅ John only      |
| City Supermarket | "Do you have organic milk?"   | `super_xyz456`  | 2                  | ✅ Sarah only     |
| Tech Store       | "What's the laptop warranty?" | `store_def789`  | 3                  | ✅ Mike only      |

#### 3️⃣ How Isolation is Enforced

**At Database Level:**

```sql
-- John's dashboard can ONLY see Pizza Restaurant conversations
SELECT * FROM conversations WHERE tenant_id = 1

-- Sarah's dashboard can ONLY see Supermarket conversations
SELECT * FROM conversations WHERE tenant_id = 2
```

**At API Level (AuthFilter.java):**

```java
1. Verify JWT → Who is this agent?
2. Check tenant_memberships → Is this agent authorized for this tenant?
3. If NO → Return 403 Forbidden
4. If YES → Allow, but auto-filter all queries by tenant_id
```

**At Relay Level (Router.java):**

```java
// When broadcasting message event:
relay.publish(
  tenantId: 1,           // Only notify agents of tenant 1
  conversationId: 123,
  event: "new_message"
)
// Result: Only John receives notification
```

### 🔐 Security Benefits

✅ **Data Isolation:** Businesses never see each other's data  
✅ **Public Key Safety:** `tenant_public_key` can be in website source (not a secret)  
✅ **Agent Authorization:** Backend verifies agent membership before showing data  
✅ **Automatic Filtering:** Every query automatically scoped by `tenant_id`

---

## 🗂️ Project Structure

```
mini-intercom/
├── frontend/          # Next.js app (demo + widget + agent dashboard)
├── backend/           # Java Servlet API (Tomcat)
├── relay/             # Java TCP relay server (sockets + threads)
├── supabase/          # SQL migrations + seed data
├── docs/              # Architecture + API notes
├── .env.example       # Environment template
└── README.md
```

### 📁 `frontend/` — Next.js (TypeScript)

```
frontend/
├── src/
│   ├── app/
│   │   ├── page.tsx             # Main landing page with system overview, features, embed instructions, and multi-tenant demo
│   │   ├── widget/page.tsx      # Widget UI (iframe target)
│   │   └── dashboard/page.tsx   # Agent inbox + chat panel
│   │
│   ├── components/
│   │   ├── ChatWindow.tsx       # Message list + composer
│   │   ├── ConversationList.tsx # Inbox sidebar
│   │   └── AuthContext.tsx      # Supabase Auth Context (login/logout, session management)
│   │
│   └── lib/
│       ├── supabase.ts          # Supabase client setup + Auth helpers (signIn, signOut, getSession)
│       └── api.ts               # Calls servlet endpoints + token injection + error handling
│
├── public/
│   └── widget-loader.js         # Optional: inject iframe script
│
├── middleware.ts                # Protect dashboard routes
├── package.json
└── .env.local                   # Frontend env (gitignored)
```

**Design Principle:** Minimal nesting, only essential components, fast onboarding.

---

### 📁 `backend/` — Java Servlet API (Tomcat)

```
backend/
├── src/main/java/com/minintercom/
│   ├── filters/
│   │   └── AuthFilter.java         # Verify JWT + tenant membership
│   │
│   ├── servlets/
│   │   ├── HealthServlet.java      # Health check endpoint
│   │   ├── ConversationsServlet.java
│   │   ├── MessagesServlet.java
│   │   └── PollServlet.java        # Long-poll using BlockingQueue
│   │
│   ├── services/
│   │   ├── JwtService.java         # JWT verification + claims
│   │   ├── ChatService.java        # DB operations (tenant-scoped)
│   │   ├── PollQueueService.java   # Queue routing per tenant/conversation
│   │   └── RelayClient.java        # TCP client to relay server
│   │
│   └── db/
│       └── Db.java                 # JDBC pool (Hikari) + helpers
│
├── src/main/resources/
│   └── application.properties      # DB + relay + auth config
│
├── pom.xml
└── target/                         # Build output (ignored)
```

**Design Principle:** Small servlet surface, business logic in services.

---

### 📁 `relay/` — Java TCP Relay Server

```
relay/
├── src/main/java/com/minintercom/relay/
│   ├── RelayServer.java       # ServerSocket accept loop
│   ├── ClientHandler.java     # Per-client handler (threaded)
│   ├── Router.java            # Routes events by tenant/conversation
│   └── Protocol.java          # Simple message framing/parsing
│
├── pom.xml
└── target/
```

**Design Principle:** Clean showcase of Sockets + Threads with thread pools and concurrency-safe routing.

---

### 📁 `supabase/` — Database Assets

```
supabase/
├── migrations/
│   └── 001_init.sql    # tenants, memberships, conversations, messages
└── seed/
    └── seed.sql        # demo tenant + agent memberships
```

---

### 📁 `docs/` — Lightweight Documentation

```
docs/
├── architecture.md    # 1-page system overview
└── api.md            # Endpoint specs + payload shapes
```

---

## 🗄️ Database Structure

**Core Tables (MVP):**

### 🏢 `tenants`

Stores business/account records.

| Column       | Type      | Description                         |
| ------------ | --------- | ----------------------------------- |
| `id`         | UUID      | Primary key                         |
| `name`       | VARCHAR   | Business name (e.g., "Joe's Pizza") |
| `public_key` | VARCHAR   | Widget embed key (safe to expose)   |
| `status`     | VARCHAR   | active/inactive                     |
| `created_at` | TIMESTAMP | -                                   |

---

### 👥 `tenant_memberships`

Authorizes agents for specific tenants.

| Column       | Type      | Description              |
| ------------ | --------- | ------------------------ |
| `id`         | UUID      | Primary key              |
| `tenant_id`  | UUID      | FK to tenants            |
| `user_id`    | UUID      | Agent's Supabase Auth ID |
| `role`       | VARCHAR   | Always "agent" for MVP   |
| `created_at` | TIMESTAMP | -                        |

**Composite unique constraint:** `(tenant_id, user_id)`

---

### 💬 `conversations`

Represents visitor chat threads.

| Column         | Type      | Description                   |
| -------------- | --------- | ----------------------------- |
| `id`           | UUID      | Primary key                   |
| `tenant_id`    | UUID      | FK to tenants                 |
| `visitor_name` | VARCHAR   | Optional visitor display name |
| `status`       | VARCHAR   | open/closed                   |
| `created_at`   | TIMESTAMP | -                             |

---

### 📨 `messages`

Individual chat messages.

| Column            | Type      | Description                                      |
| ----------------- | --------- | ------------------------------------------------ |
| `id`              | UUID      | Primary key                                      |
| `tenant_id`       | UUID      | FK to tenants (denormalized for fast filtering)  |
| `conversation_id` | UUID      | FK to conversations                              |
| `sender_type`     | VARCHAR   | "visitor" or "agent"                             |
| `sender_id`       | UUID      | Nullable (null for visitors, user_id for agents) |
| `body`            | TEXT      | Message content                                  |
| `created_at`      | TIMESTAMP | -                                                |

**Key Rule:** Every record includes `tenant_id` for complete isolation.

---

## 🔐 Authentication & Authorization

Simple two-role system: **Visitor** (no auth) and **Agent** (requires auth).

### Authentication (Who are you?)

- Agent logs in via Supabase Auth
- Frontend receives JWT token
- Token sent with every API request

### Authorization (Can you access this tenant?)

1. `AuthFilter.java` verifies JWT signature
2. Extracts `user_id` from JWT claims
3. Checks `tenant_memberships` table:
   ```sql
   SELECT * FROM tenant_memberships
   WHERE tenant_id = ? AND user_id = ?
   ```
4. **If exists:** Allow request, auto-scope by `tenant_id`
5. **If not exists:** Return `403 Forbidden`

**No Admin UI Needed:** Memberships seeded directly for demo/class presentation.

---

## 🔄 Real-Time Updates Design

### Why Long-Polling (Not WebSockets)?

✅ Works everywhere (no browser compatibility issues)  
✅ Simple to implement with servlets  
✅ Clear demonstration of thread safety (blocking + timeouts)  
✅ Reliable fallback for restrictive networks

### Queue Model Architecture

```
┌──────────────┐
│ PollServlet  │
│              │
│ 1. Request   │──┐
│    arrives   │  │
│              │  │  ┌─────────────────────┐
│ 2. Get queue │◄─┼──│ PollQueueService    │
│    for       │  │  │                     │
│    tenant    │  │  │ • Queue per tenant  │
│              │  │  │ • BlockingQueue     │
│ 3. Wait on   │  │  │ • Thread-safe       │
│    queue.    │  │  └─────────────────────┘
│    poll()    │  │           ▲
│    with      │  │           │
│    timeout   │  │           │ notify()
│              │  │           │
│ 4. Released! │◄─┘  ┌────────┴─────────┐
│              │     │ MessagesServlet  │
│ 5. Return    │     │                  │
│    success   │     │ New msg saved    │
└──────────────┘     └──────────────────┘
```

**Flow:**

1. Dashboard calls `/poll?tenant_id=1`
2. `PollServlet` gets the queue for tenant 1
3. Thread blocks on `queue.poll(30, SECONDS)`
4. When new message arrives:
   - `MessagesServlet` saves to DB
   - Publishes event to relay
   - Notifies queue
5. Blocked thread wakes immediately
6. Returns success, dashboard fetches messages

---

### Relay Integration (Sockets + Threads)

```
┌─────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ MessagesServlet │      │   Relay Server   │      │  PollServlet     │
│                 │      │                  │      │  (waiting)       │
│ 1. Save msg     │      │ • ServerSocket   │      │                  │
│                 │      │ • Thread pool    │      │ BlockingQueue    │
│ 2. Publish ────►│─────►│ • Router         │─────►│ .offer(event)    │
│    event to     │ TCP  │                  │ TCP  │                  │
│    relay        │      │ 3. Broadcast to  │      │ 4. Wake up!      │
│                 │      │    subscribers   │      │                  │
└─────────────────┘      └──────────────────┘      └──────────────────┘
```

**Benefits:**

- Demonstrates **sockets** (TCP connections)
- Demonstrates **threads** (connection handlers)
- Demonstrates **concurrency** (safe routing under load)

---

## 🌐 API Overview

### Public Endpoints (Widget)

| Method | Endpoint                      | Description              | Auth                |
| ------ | ----------------------------- | ------------------------ | ------------------- |
| `POST` | `/conversations`              | Create/open conversation | `tenant_public_key` |
| `POST` | `/messages`                   | Send visitor message     | `tenant_public_key` |
| `GET`  | `/messages?conversation_id=X` | Fetch history            | `tenant_public_key` |
| `GET`  | `/poll?conversation_id=X`     | Long-poll for updates    | `tenant_public_key` |

---

### Protected Endpoints (Agent Dashboard)

| Method | Endpoint                     | Description               | Auth             |
| ------ | ---------------------------- | ------------------------- | ---------------- |
| `GET`  | `/tenants/mine`              | Get my tenant memberships | JWT              |
| `GET`  | `/conversations?tenant_id=X` | List conversations        | JWT + membership |
| `POST` | `/messages`                  | Send agent reply          | JWT + membership |
| `GET`  | `/poll?tenant_id=X`          | Long-poll for updates     | JWT + membership |

**Design Rule:** Every request is tenant-scoped (either by `tenant_public_key` or verified membership).

---

## 🧪 Local Setup Guide

### Prerequisites

#### System Requirements

| Tool    | Version   | Check Command         | Installation                                    |
| ------- | --------- | --------------------- | ----------------------------------------------- |
| Java    | 21 (LTS)  | `java --version`      | [adoptium.net](https://adoptium.net/)           |
| Maven   | 3.8+      | `mvn --version`       | [maven.apache.org](https://maven.apache.org/)   |
| Tomcat  | 9.0+      | `catalina.sh version` | [tomcat.apache.org](https://tomcat.apache.org/) |
| Node.js | 18+ (LTS) | `node --version`      | [nodejs.org](https://nodejs.org/)               |
| npm     | 9+        | `npm --version`       | Included with Node.js                           |
| Git     | 2.0+      | `git --version`       | [git-scm.com](https://git-scm.com/)             |

#### Project Dependencies

| Framework/Service | Version | Check Command        | Installation                          |
| ----------------- | ------- | -------------------- | ------------------------------------- |
| Next.js           | 16      | `npx next --version` | [nextjs.org](https://nextjs.org/)     |
| TypeScript        | 5+      | `npx tsc --version`  | Installed via `npm install`           |
| Supabase          | Cloud   | N/A (Web service)    | [supabase.com](https://supabase.com/) |

**Notes:**

- Next.js and TypeScript are installed as project dependencies via `npm install` in the frontend directory
- Supabase is a cloud service - create a free project for database and authentication

---

### Run Order

#### 1️⃣ Start Relay Server

```bash
cd relay
mvn clean package
java -jar target/relay-server.jar
```

✅ Server listening on `localhost:9090`

---

#### 2️⃣ Start Backend (Tomcat)

```bash
cd backend
mvn clean package
# Copy target/backend.war to Tomcat webapps/
# Start Tomcat
```

✅ API available at `http://localhost:8080/backend`

---

#### 3️⃣ Start Frontend

```bash
cd frontend
npm install
npm run dev
```

✅ Frontend running at `http://localhost:3000`

---

### Sanity Test Checklist

- [ ] Widget creates conversation and sends message
- [ ] Agent logs in and sees conversation in inbox
- [ ] Agent replies and visitor sees it quickly (< 2 seconds)
- [ ] Data is correctly isolated per tenant (agents only see their tenant's data)
- [ ] Long-poll works (updates appear without refresh)

---

## ✅ Milestones

### Milestone 1 — Skeleton ⚙️

- [ ] Frontend pages exist (demo/widget/dashboard)
- [ ] Backend health endpoint responds
- [ ] Relay server starts and accepts TCP connections

---

### Milestone 2 — Auth Gate 🔐

- [ ] Supabase login works in dashboard
- [ ] Backend verifies JWT and rejects invalid tokens
- [ ] Tenant membership check works (403 for unauthorized)

---

### Milestone 3 — Chat MVP 💬

- [ ] Conversations stored in Supabase Postgres
- [ ] Messages stored and retrieved correctly
- [ ] Widget can send and view history
- [ ] Dashboard shows inbox and can reply

---

### Milestone 4 — Near Real-Time ⚡

- [ ] Long-poll endpoint blocks and releases on new message
- [ ] Widget updates without refresh
- [ ] Dashboard updates without refresh
- [ ] Timeout works correctly (30 seconds, then retry)

---

### Milestone 5 — Relay Integration 🔌

- [ ] Backend publishes events to relay server via TCP
- [ ] Relay broadcasts to subscribed clients correctly
- [ ] No concurrency bugs under parallel testing (multiple tenants/conversations)

---

### Milestone 6 — Embed Anywhere 🌍 (Bonus)

- [ ] `widget-loader.js` injects iframe on external pages
- [ ] Tenant public key correctly routes messages
- [ ] Works on different domains (CORS configured)

---

## 🎬 Demo and Presentation Script

### Suggested Flow:

1. **Introduction** (30 seconds)

   - "I built a multi-tenant live chat system like Intercom"
   - Show the tech stack slide

2. **Demo Landing Page** (30 seconds)

   - Show demo page with embed code for different businesses

3. **Visitor Experience** (1 minute)

   - Open Pizza Restaurant's website
   - Widget opens, send message: "Do you deliver?"
   - Show it appears in dashboard almost instantly

4. **Agent Experience** (1 minute)

   - Log in as John (Pizza Restaurant agent)
   - Show inbox with the visitor's conversation
   - Reply: "Yes! Free delivery over $20"
   - Show visitor receives reply without refresh

5. **Multi-Tenant Isolation** (1 minute)

   - Open Supermarket widget, send different message
   - Log in as Sarah (different agent)
   - Show she ONLY sees Supermarket conversations
   - Show John still ONLY sees Pizza conversations

6. **Technical Deep Dive** (2 minutes)

   - **Servlets:** Show `AuthFilter.java` + `PollServlet.java`
   - **Threads:** Explain BlockingQueue for long-poll
   - **Sockets:** Show `RelayServer.java` TCP connections

7. **Database** (30 seconds)
   - Open Supabase, show conversations and messages tables
   - Point out `tenant_id` on every record

---

## 📋 Development Guidelines

- ✅ Keep MVP small, avoid feature creep
- ✅ Commit frequently with clear, descriptive messages
- ✅ Every milestone must be demoable before moving to next
- ✅ Always tenant-scope data access (security first!)
- ✅ Test thoroughly at each milestone
- ✅ Document any assumptions or simplifications

---

## 🎯 Goals

✅ Clear demonstration of **Servlets, Sockets, and Threads**  
✅ Multi-tenant isolation with minimal schema  
✅ Stable demo flow (visitor → agent reply → fast updates)  
✅ Clean folder structure with minimal file sprawl

_Last Updated: December 2025_  
_Java Version: 21 (LTS)_  
_Project Type: Advanced Programming (Individual)_
