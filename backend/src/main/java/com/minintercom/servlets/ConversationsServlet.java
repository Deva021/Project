package com.minintercom.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.dto.Conversation;
import com.minintercom.services.ConversationService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Servlet for handling chat conversation-related API requests.
 * Provides endpoints for creating new conversations and listing existing ones for a tenant.
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

        // In a real application, tenantId would be set by an AuthFilter in TenantContext
        // For this basic servlet, we'll assume a tenantId is available from request attribute (set by filter) or context
        UUID tenantId = (UUID) req.getAttribute("tenantId"); // Assuming AuthFilter sets this
        if (tenantId == null) {
            // For testing purposes, or if no AuthFilter is present
            // In a real secure app, this would be a 401 or 403
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().println("{\"error\":\"Tenant ID not found\"}");
            return;
        }

        try {
            List<Conversation> conversations = conversationService.getConversationsByTenantId(tenantId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println(objectMapper.writeValueAsString(conversations));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Handles POST requests to /conversations.
     * Creates a new conversation. The request body should contain a JSON object with at least a 'title'.
     *
     * @param req  The HttpServletRequest object.
     * @param resp The HttpServletResponse object.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Read the request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();

            // Parse the request body into a map or DTO
            // Assuming the request body contains "title" and "visitor_name"
            Conversation newConversation = objectMapper.readValue(requestBody, Conversation.class);

            // In a real application, tenantId would be set by an AuthFilter in TenantContext
            // For this basic servlet, we'll use a placeholder UUID or get from a test context
            UUID tenantId = UUID.randomUUID(); // Placeholder for testing
            newConversation.setTenantId(tenantId);

            Conversation createdConversation = conversationService.createConversation(newConversation, null); // visitor_name is not handled in service yet

            if (createdConversation != null) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().println(objectMapper.writeValueAsString(createdConversation));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println("{\"error\":\"Failed to create conversation\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
