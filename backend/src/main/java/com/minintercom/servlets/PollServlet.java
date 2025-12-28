package com.minintercom.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.realtime.events.RealtimeEvent;
import com.minintercom.realtime.pollqueue.PollQueueService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Servlet for handling long-polling real-time event requests.
 * Provides an endpoint for clients to receive events for a specific tenant and conversation.
 */
public class PollServlet extends HttpServlet {

    private final PollQueueService pollQueueService;

    public PollServlet() {
        // Default constructor for servlet container instantiation
        this.pollQueueService = PollQueueService.getInstance();
    }

    // Constructor for dependency injection in tests
    public PollServlet(PollQueueService pollQueueService) {
        this.pollQueueService = pollQueueService;
    }

    /**
     * Handles GET requests to /poll.
     * Implements a long-polling mechanism to deliver real-time events.
     * Requires 'tenantId' and 'conversationId' as request parameters.
     *
     * @param req  The HttpServletRequest object.
     * @param resp The HttpServletResponse object.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs during request/response processing or polling.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();

        String tenantIdStr = req.getParameter("tenantId");
        String conversationIdStr = req.getParameter("conversationId");

        if (tenantIdStr == null || tenantIdStr.isEmpty() || conversationIdStr == null || conversationIdStr.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Missing tenantId or conversationId parameter\"}");
            return;
        }

        try {
            UUID tenantId = UUID.fromString(tenantIdStr);
            UUID conversationId = UUID.fromString(conversationIdStr);

            // Long-polling timeout
            long timeoutMs = 30000; // 30 seconds
            List<RealtimeEvent> events = pollForEvents(tenantId, conversationId, timeoutMs, TimeUnit.MILLISECONDS);

            if (events.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 No Content
                // According to spec: Still send JSON header even for empty body for consistency
                resp.getWriter().println("[]");
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().println(objectMapper.writeValueAsString(events));
            }

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("{\"error\":\"Invalid ID format: " + e.getMessage() + "\"}");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Polling interrupted", e); // Wrap it in IOException
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Helper method to poll for real-time events.
     * This method exists primarily to satisfy the Java compiler's strictness about InterruptedException
     * in the doGet method, while pollQueueService.poll is indeed declared to throw it.
     *
     * @param tenantId The UUID of the tenant.
     * @param conversationId The UUID of the conversation.
     * @param timeout The maximum time to wait for events.
     * @param unit The time unit for the timeout.
     * @return A list of RealtimeEvents.
     * @throws InterruptedException If the polling thread is interrupted.
     */
    private List<RealtimeEvent> pollForEvents(UUID tenantId, UUID conversationId, long timeout, TimeUnit unit) throws InterruptedException {
        return pollQueueService.poll(tenantId, conversationId, timeout, unit);
    }
}
