package com.minintercom.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.dto.Message;
import com.minintercom.services.MessageService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Servlet for handling chat message-related API requests.
 * Provides endpoints for sending messages and retrieving message history for a conversation.
 */
public class MessagesServlet extends HttpServlet {

    private final MessageService messageService;

    public MessagesServlet() {
        // Default constructor for servlet container instantiation
        this.messageService = new MessageService();
    }

    // Constructor for dependency injection in tests
    public MessagesServlet(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Handles GET requests to /messages.
     * Retrieves the message history for a specified conversation ID.
     * Requires 'conversationId' as a request parameter.
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

        String conversationIdStr = req.getParameter("conversationId");
        if (conversationIdStr == null || conversationIdStr.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Missing conversationId parameter\"}");
            return;
        }

        try {
            UUID conversationId = UUID.fromString(conversationIdStr);
            // In a real application, you'd verify tenant access via AuthFilter/TenantContext
            List<Message> messages = messageService.getMessageHistory(conversationId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println(objectMapper.writeValueAsString(messages));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Invalid conversationId format\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Handles POST requests to /messages.
     * Sends a new message to a conversation. The request body should be a JSON object
     * containing 'conversationId', 'content', and optionally 'senderId' and 'senderType'.
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
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();

            // Expected JSON: { "conversationId": "...", "content": "...", "senderId": "...", "senderType": "..." }
            // For this basic servlet, we'll extract directly.
            // In a more robust system, a dedicated DTO for request body would be used.
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> jsonMap = objectMapper.readValue(requestBody, java.util.Map.class);

            String conversationIdStr = (String) jsonMap.get("conversationId");
            String content = (String) jsonMap.get("content");
            String senderIdStr = (String) jsonMap.get("senderId");
            String senderType = (String) jsonMap.get("senderType");

            if (conversationIdStr == null || content == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("{\"error\":\"Missing conversationId or content\"}");
                return;
            }

            UUID conversationId = UUID.fromString(conversationIdStr);
            UUID senderId = (senderIdStr != null) ? UUID.fromString(senderIdStr) : null;
            // For senderType, if not provided, assume 'visitor' or handle according to spec
            if (senderType == null || senderType.isEmpty()) {
                senderType = "visitor"; // Default to visitor if not specified
            }


            Message createdMessage = messageService.sendMessage(conversationId, senderId, senderType, content);

            if (createdMessage != null) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().println(objectMapper.writeValueAsString(createdMessage));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println("{\"error\":\"Failed to send message\"}");
            }

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Invalid ID format: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // More general for parsing or other errors
            resp.getWriter().println("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
