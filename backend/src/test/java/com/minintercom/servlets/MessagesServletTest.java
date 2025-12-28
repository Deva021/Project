package com.minintercom.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.dto.Message;
import com.minintercom.services.MessageService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessagesServletTest {

    private MessagesServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private MessageService messageService;
    private StringWriter stringWriter;
    private PrintWriter writer;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws IOException {
        messageService = mock(MessageService.class);
        servlet = new MessagesServlet(messageService); // Inject mock service
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        objectMapper = new ObjectMapper();

        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void testDoPost() throws ServletException, IOException {
        // Mock request body for sending a message
        UUID conversationId = UUID.randomUUID();
        String requestBody = String.format("{\"conversationId\":\"%s\",\"content\":\"Hello\"}", conversationId.toString());
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        when(request.getContentType()).thenReturn("application/json");

        // Mock MessageService behavior
        Message sentMessage = new Message();
        sentMessage.setId(UUID.randomUUID());
        sentMessage.setConversationId(conversationId);
        sentMessage.setText("Hello");
        when(messageService.sendMessage(any(UUID.class), any(), anyString(), anyString())).thenReturn(sentMessage);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(response).setContentType("application/json");
        assertEquals(objectMapper.writeValueAsString(sentMessage), stringWriter.toString().trim());
    }

    @Test
    public void testDoGet() throws ServletException, IOException {
        // Mock request parameters
        UUID conversationId = UUID.randomUUID();
        when(request.getParameter("conversationId")).thenReturn(conversationId.toString());

        // Mock MessageService behavior
        List<Message> messages = Arrays.asList(
                new Message(UUID.randomUUID(), conversationId, UUID.randomUUID(), "Hello visitor"),
                new Message(UUID.randomUUID(), conversationId, UUID.randomUUID(), "Hello agent")
        );
        when(messageService.getMessageHistory(any(UUID.class))).thenReturn(messages);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        assertEquals(objectMapper.writeValueAsString(messages), stringWriter.toString().trim());
    }
}
