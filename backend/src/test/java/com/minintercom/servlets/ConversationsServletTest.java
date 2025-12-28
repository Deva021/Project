package com.minintercom.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.dto.Conversation;
import com.minintercom.services.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ConversationsServletTest {

    private ConversationsServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ConversationService conversationService;
    private StringWriter stringWriter;
    private PrintWriter writer;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws IOException {
        conversationService = mock(ConversationService.class);
        servlet = new ConversationsServlet(conversationService); // Inject mock service
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        objectMapper = new ObjectMapper();

        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void testDoPost() throws ServletException, IOException {
        // Mock request body
        String requestBody = "{\"title\":\"Test Conversation\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(request.getContentType()).thenReturn("application/json");

        // Mock ConversationService behavior
        UUID mockedTenantId = UUID.randomUUID(); // Mock a tenant ID
        Conversation newConversation = new Conversation();
        newConversation.setId(UUID.randomUUID());
        newConversation.setTitle("Test Conversation");
        newConversation.setTenantId(mockedTenantId); // Set the mocked tenant ID
        when(conversationService.createConversation(any(Conversation.class), eq(null))).thenReturn(newConversation); // Pass null for visitorName

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(response).setContentType("application/json");
        assertEquals(objectMapper.writeValueAsString(newConversation), stringWriter.toString().trim());
    }

    @Test
    public void testDoGet() throws ServletException, IOException {
        // Mock conversation service behavior
        UUID tenantId = UUID.randomUUID(); // Assume tenant is set by AuthFilter in a real scenario
        List<Conversation> conversations = Arrays.asList(
                new Conversation(UUID.randomUUID(), tenantId, "Conv 1"),
                new Conversation(UUID.randomUUID(), tenantId, "Conv 2")
        );
        when(conversationService.getConversationsByTenantId(any(UUID.class))).thenReturn(conversations);

        // Mock AuthFilter setting tenantId (simulated)
        // In a real application, AuthFilter would set this via TenantContext
        // For unit test, we can directly mock the service interaction
        when(request.getAttribute("tenantId")).thenReturn(tenantId); // This is a placeholder. Real tenantId would come from TenantContext

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        assertEquals(objectMapper.writeValueAsString(conversations), stringWriter.toString().trim());
    }
}
