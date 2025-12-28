package com.minintercom.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.realtime.events.RealtimeEvent;
import com.minintercom.realtime.pollqueue.PollQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PollServletTest {

    private PollServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PollQueueService pollQueueService;
    private StringWriter stringWriter;
    private PrintWriter writer;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws IOException {
        pollQueueService = mock(PollQueueService.class);
        servlet = new PollServlet(pollQueueService); // Inject mock service
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        objectMapper = new ObjectMapper();

        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void testDoGetWithEvents() throws ServletException, IOException, InterruptedException {
        // Mock request parameters
        UUID tenantId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(request.getParameter("tenantId")).thenReturn(tenantId.toString());
        when(request.getParameter("conversationId")).thenReturn(conversationId.toString());

        // Mock PollQueueService behavior: return events immediately
        List<RealtimeEvent> events = Arrays.asList(
                new RealtimeEvent("TEST_EVENT", tenantId, conversationId) { // Added missing 'type' argument
                    @Override
                    public String getType() { return "TEST_EVENT"; }
                }
        );
        when(pollQueueService.poll(eq(tenantId), eq(conversationId), anyLong(), any(TimeUnit.class))).thenReturn(events);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        assertEquals(objectMapper.writeValueAsString(events), stringWriter.toString().trim());
    }

    @Test
    public void testDoGetWithTimeout() throws ServletException, IOException, InterruptedException {
        // Mock request parameters
        UUID tenantId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(request.getParameter("tenantId")).thenReturn(tenantId.toString());
        when(request.getParameter("conversationId")).thenReturn(conversationId.toString());

        // Mock PollQueueService behavior: return empty list after timeout
        when(pollQueueService.poll(eq(tenantId), eq(conversationId), anyLong(), any(TimeUnit.class))).thenReturn(Arrays.asList());

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 No Content
        verify(response).setContentType("application/json"); // Still send JSON header even for empty body
        assertEquals("[]", stringWriter.toString().trim());
    }

    @Test
    public void testDoGetMissingParameters() throws ServletException, IOException, InterruptedException { // Added InterruptedException
        // Missing tenantId
        when(request.getParameter("conversationId")).thenReturn(UUID.randomUUID().toString());
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);

        // Missing conversationId
        when(request.getParameter("tenantId")).thenReturn(UUID.randomUUID().toString());
        when(request.getParameter("conversationId")).thenReturn(null);
        stringWriter.getBuffer().setLength(0); // Clear previous output
        servlet.doGet(request, response);
        verify(response, times(2)).setStatus(HttpServletResponse.SC_BAD_REQUEST); // Called twice
    }
}
