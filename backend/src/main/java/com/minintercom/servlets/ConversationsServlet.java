package com.minintercom.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.dto.Conversation;
import com.minintercom.services.ConversationService;
import com.minintercom.common.TenantContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet for handling chat conversation-related API requests.
 * Provides endpoints for creating new conversations and listing existing ones
 * for a tenant.
 */
public class ConversationsServlet extends HttpServlet {

    private final ConversationService conversationService;

    public ConversationsServlet() {
        // Default constructor for servlet container instantiation
        this.conversationService = new ConversationService();
    }

    // Constructor for dependency injection in tests
    public ConversationsServlet(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * Handles GET requests to /conversations.
     * Retrieves a list of conversations for the authenticated user's tenant.
     * Requires tenantId to be set as a request attribute (e.g., by an AuthFilter).
     *
     * @param req  The HttpServletRequest object.
     * @param resp The HttpServletResponse object.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();

        // Retrieve tenantId from TenantContext, which is set by AuthFilter
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().println("{\"error\":\"Tenant ID not found in context\"}");
            return;
        }

        try {
            String status = req.getParameter("status");
            List<Conversation> conversations = conversationService.getConversationsByTenantId(tenantId, status);

            // Trigger cleanup periodically (simplified: on every list request)
            try {
                conversationService.cleanupOldClosedConversations();
            } catch (Exception e) {
                System.err.println("DEBUG: Cleanup failed (ignoring): " + e.getMessage());
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println(objectMapper.writeValueAsString(conversations));
        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch conversations for tenant: " + tenantId);
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new java.util.HashMap<>();
            error.put("error", e.getMessage());
            resp.getWriter().println(objectMapper.writeValueAsString(error));
        }
    }

    /**
     * Handles POST requests to /conversations.
     * Creates a new conversation and an initial message.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();

        String requestBody = "";
        try {
            // Read the request body for logging and parsing
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            requestBody = sb.toString();
            System.out.println("DEBUG: POST /api/conversations request body: " + requestBody);

            // Parse the request body into a Map to extract all fields
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> body = objectMapper.readValue(requestBody, java.util.Map.class);

            String title = (String) body.get("title");
            String initialMessageContent = (String) body.get("message");
            String tenantIdFromBody = (String) body.get("tenant_id");

            if (initialMessageContent == null || initialMessageContent.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("{\"error\":\"Initial message is required\"}");
                return;
            }

            // Retrieve tenantId from TenantContext
            UUID tenantId = TenantContext.getTenantId();

            // Fallback to tenant_id from body if context is missing (simplified security)
            if (tenantId == null && tenantIdFromBody != null) {
                try {
                    tenantId = UUID.fromString(tenantIdFromBody);
                    System.out.println("DEBUG: Using tenant_id from request body: " + tenantId);
                } catch (IllegalArgumentException e) {
                    System.err.println("DEBUG: Invalid tenant_id format in body: " + tenantIdFromBody);
                }
            }

            if (tenantId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().println("{\"error\":\"Tenant ID not found in context or body\"}");
                return;
            }

            // Create Conversation object
            Conversation newConversation = new Conversation();
            newConversation.setTenantId(tenantId);
            newConversation.setTitle(title);

            // Save conversation
            Conversation createdConversation = conversationService.createConversation(newConversation, null);

            if (createdConversation != null) {
                // Create initial message
                com.minintercom.services.MessageService messageService = new com.minintercom.services.MessageService();
                com.minintercom.dto.Message initialMessage = messageService.sendMessage(
                        createdConversation.getId(),
                        null,
                        "visitor",
                        initialMessageContent);

                // Prepare combined response
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("conversation", createdConversation);
                result.put("initialMessage", initialMessage);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().println(objectMapper.writeValueAsString(result));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println("{\"error\":\"Failed to create conversation\"}");
            }

        } catch (Exception e) {
            System.err.println("ERROR: Failed to process POST /api/conversations");
            System.err.println("Request Body: " + requestBody);
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new java.util.HashMap<>();
            error.put("error", e.getMessage());
            resp.getWriter().println(objectMapper.writeValueAsString(error));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Conversation ID is required\"}");
            return;
        }

        // Expected path: /api/conversations/{id}/status
        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Invalid path format\"}");
            return;
        }

        String conversationIdStr = parts[1];
        UUID conversationId;
        try {
            conversationId = UUID.fromString(conversationIdStr);
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Invalid conversation ID format\"}");
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> body = objectMapper.readValue(req.getReader(), java.util.Map.class);
            String status = (String) body.get("status");

            if (status == null || status.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("{\"error\":\"Status is required\"}");
                return;
            }

            conversationService.updateConversationStatus(conversationId, status);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("{\"success\":true}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
