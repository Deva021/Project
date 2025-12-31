package com.minintercom.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.dto.Conversation;
import com.minintercom.dto.Message;
import com.minintercom.services.ConversationService;
import com.minintercom.services.MessageService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet for handling chat-related API requests.
 */
public class ChatServlet extends HttpServlet {

    private final ConversationService conversationService = new ConversationService();
    private final MessageService messageService = new MessageService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles GET requests to list conversations or fetch message history.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/conversations - List conversations for the current tenant
            handleListConversations(req, resp);
        } else if (pathInfo.endsWith("/messages")) {
            // GET /api/conversations/{id}/messages - Fetch message history
            handleGetMessageHistory(req, resp);
        } else {
            // GET /api/conversations/{id} - Fetch single conversation details
            handleGetConversation(req, resp);
        }
    }

    private void handleListConversations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UUID tenantId = (UUID) req.getAttribute("tenantId"); // Assuming AuthFilter sets this
        if (tenantId == null) {
            tenantId = com.minintercom.common.TenantContext.getTenantId();
        }
        if (tenantId == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant ID not found");
            return;
        }
        try {
            String status = req.getParameter("status");
            List<Conversation> conversations = conversationService.getConversationsByTenantId(tenantId, status);

            // Trigger cleanup periodically
            try {
                conversationService.cleanupOldClosedConversations();
            } catch (Exception e) {
                System.err.println("DEBUG: Cleanup failed (ignoring): " + e.getMessage());
            }

            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getWriter(), conversations);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void handleGetConversation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            UUID conversationId = UUID.fromString(parts[1]);
            Conversation conversation = conversationService.getConversationById(conversationId);
            if (conversation != null) {
                resp.setContentType("application/json");
                objectMapper.writeValue(resp.getWriter(), conversation);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid conversation ID format");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void handleGetMessageHistory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            UUID conversationId = UUID.fromString(parts[1]);
            List<Message> messages = messageService.getMessageHistory(conversationId);
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getWriter(), messages);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid conversation ID format");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    /**
     * Handles POST requests to create conversations or send messages.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // POST /api/conversations - Create a new conversation
            handleCreateConversation(req, resp);
        } else if (pathInfo.endsWith("/messages")) {
            // POST /api/conversations/{id}/messages - Send a message
            handleSendMessage(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleCreateConversation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> body = objectMapper.readValue(req.getReader(), Map.class);
        String tenantIdStr = body.get("tenant_id");
        String title = body.get("title"); // Assuming title is sent
        String initialMessageContent = body.get("message");

        if (tenantIdStr == null || initialMessageContent == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing tenant_id or message");
            return;
        }

        try {
            UUID tenantId = UUID.fromString(tenantIdStr);
            Conversation newConversation = new Conversation();
            newConversation.setTenantId(tenantId);
            newConversation.setTitle(title);

            Conversation conversation = conversationService.createConversation(newConversation, null); // visitor_name
                                                                                                       // not handled
                                                                                                       // here yet

            if (conversation != null) {
                Message message = messageService.sendMessage(conversation.getId(), null, "visitor",
                        initialMessageContent);

                Map<String, Object> result = new HashMap<>();
                result.put("conversation", conversation);
                result.put("initialMessage", message);

                resp.setContentType("application/json");
                objectMapper.writeValue(resp.getWriter(), result);
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create conversation");
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant_id format");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void handleSendMessage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            UUID conversationId = UUID.fromString(parts[1]);
            Map<String, String> body = objectMapper.readValue(req.getReader(), Map.class);
            String text = body.get("text");
            String senderType = body.get("sender_type"); // 'visitor' or 'agent'
            String senderIdStr = body.get("sender_id");

            if (text == null || senderType == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing text or sender_type");
                return;
            }

            UUID senderId = (senderIdStr != null) ? UUID.fromString(senderIdStr) : null;
            Message message = messageService.sendMessage(conversationId, senderId, senderType, text);

            if (message != null) {
                resp.setContentType("application/json");
                objectMapper.writeValue(resp.getWriter(), message);
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to send message");
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    /**
     * Handles PUT requests to update conversation status.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.endsWith("/status")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            UUID conversationId = UUID.fromString(parts[1]);
            Map<String, String> body = objectMapper.readValue(req.getReader(), Map.class);
            String status = body.get("status");

            if (status == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing status");
                return;
            }

            conversationService.updateConversationStatus(conversationId, status);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid conversation ID format");
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
