package com.minintercom.realtime.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.common.TenantContext;
import com.minintercom.realtime.events.RealtimeEvent;
import com.minintercom.realtime.pollqueue.PollQueueService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * Servlet for long-polling real-time events.
 */
@WebServlet("/api/realtime/poll")
public class RealtimeServlet extends HttpServlet {

    private final PollQueueService pollQueueService = PollQueueService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final long DEFAULT_TIMEOUT_MS = 30000;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant context missing");
            return;
        }

        String conversationIdStr = req.getParameter("conversation_id");
        String timeoutStr = req.getParameter("timeout");
        long timeoutMs = (timeoutStr != null) ? Long.parseLong(timeoutStr) : DEFAULT_TIMEOUT_MS;

        List<RealtimeEvent> events;
        if (conversationIdStr != null && !conversationIdStr.isEmpty()) {
            try {
                UUID conversationId = UUID.fromString(conversationIdStr);
                events = pollQueueService.poll(tenantId, conversationId, timeoutMs, TimeUnit.MILLISECONDS);
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid conversation_id format");
                return;
            }
        } else {
            events = pollQueueService.pollTenant(tenantId, timeoutMs, TimeUnit.MILLISECONDS);
        }

        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getWriter(), events);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant context missing");
            return;
        }

        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(req.getReader());
            UUID conversationId = UUID.fromString(node.get("conversation_id").asText());
            UUID agentId = node.has("agent_id") ? UUID.fromString(node.get("agent_id").asText()) : null;
            boolean isTyping = node.get("is_typing").asBoolean();

            com.minintercom.realtime.events.AgentTypingEvent event = new com.minintercom.realtime.events.AgentTypingEvent(
                    tenantId,
                    conversationId,
                    agentId,
                    isTyping);
            com.minintercom.realtime.client.RealtimeClient.getInstance().publish(event);

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request body: " + e.getMessage());
        }
    }
}
