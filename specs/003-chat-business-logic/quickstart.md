# Quickstart: Chat Business Logic API

This guide provides `curl` examples for interacting with the new chat API.

**Note**: Replace `{JWT}` with a valid JSON Web Token for an authenticated agent and `{CONVERSATION_ID}` with a valid conversation UUID.

---

### 1. Create a new conversation (as a visitor)

This is a public endpoint.

```bash
curl -X POST http://localhost:8080/api/conversations \
-H "Content-Type: application/json" \
-d '{
  "initialMessage": "Hello, I need help with my order."
}'
```

### 2. List conversations (as an agent)

This is a protected endpoint.

```bash
curl -X GET http://localhost:8080/api/conversations \
-H "Authorization: Bearer {JWT}"
```

### 3. Send a reply (as an agent)

This endpoint is protected when an agent sends a message.

```bash
curl -X POST http://localhost:8080/api/conversations/{CONVERSATION_ID}/messages \
-H "Authorization: Bearer {JWT}" \
-H "Content-Type: application/json" \
-d '{
  "text": "Hello, I can help you with that. What is your order number?"
}'
```

### 4. Get message history

This endpoint is public/protected.

```bash
curl -X GET http://localhost:8080/api/conversations/{CONVERSATION_ID}/messages
```

### 5. Update conversation status (as an agent)

This is a protected endpoint.

```bash
curl -X PUT http://localhost:8080/api/conversations/{CONVERSATION_ID}/status \
-H "Authorization: Bearer {JWT}" \
-H "Content-Type: application/json" \
-d '{
  "status": "CLOSED"
}'
```
