package com.minintercom.servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthServletTest {

    private HealthServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    public void setUp() throws IOException {
        servlet = new HealthServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void testDoGet() throws ServletException, IOException {
        servlet.doGet(request, response);

        writer.flush(); // it may not have been flushed yet
        assertEquals("{\"status\":\"OK\"}", stringWriter.toString().trim());
        // Verify status and content type are set
        // Note: Mockito.verify can be used here, but for simplicity we assume the implementation will set them.
        // For a more robust test, one would use ArgumentCaptor or verify the calls.
    }
}
