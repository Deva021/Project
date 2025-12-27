package com.minintercom.realtime.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.realtime.events.RealtimeEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Backend client for publishing events to the RealtimeServer.
 */
public class RealtimeClient {

    private static final Logger LOGGER = Logger.getLogger(RealtimeClient.class.getName());
    private static final String HOST;
    private static final int PORT;
    private static final String SYSTEM_KEY;
    private static final int RETRY_DELAY_MS = 5000;

    static {
        java.util.Properties prop = new java.util.Properties();
        try (java.io.InputStream input = RealtimeClient.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                prop.load(input);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        HOST = prop.getProperty("relay.host", "localhost");
        PORT = Integer.parseInt(prop.getProperty("relay.port", "9090"));
        // Use EnvLoader to check environment or .env file
        SYSTEM_KEY = com.minintercom.common.EnvLoader.get("RELAY_SYSTEM_KEY");
    }

    private static final RealtimeClient INSTANCE = new RealtimeClient();

    private final BlockingQueue<RealtimeEvent> eventQueue = new LinkedBlockingQueue<>();
    private Socket socket;
    private PrintWriter writer;
    private boolean running = true;

    private RealtimeClient() {
        startSenderThread();
    }

    public static RealtimeClient getInstance() {
        return INSTANCE;
    }

    /**
     * Publishes an event to the real-time server.
     */
    public void publish(RealtimeEvent event) {
        eventQueue.offer(event);
    }

    private void startSenderThread() {
        new Thread(() -> {
            while (running) {
                try {
                    ensureConnected();
                    RealtimeEvent event = eventQueue.take();
                    String json = objectMapper.writeValueAsString(event);
                    writer.println(json);

                    if (writer.checkError()) {
                        throw new IOException("Socket write error");
                    }
                } catch (IOException | InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Error sending event, will retry: " + e.getMessage());
                    closeConnection();
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, "RealtimeClient-Sender").start();
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private synchronized void ensureConnected() throws IOException {
        if (socket == null || socket.isClosed() || writer == null) {
            socket = new Socket(HOST, PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Send initial CONNECT message (internal system client)
            java.util.Map<String, String> connectMsg = new java.util.HashMap<>();
            connectMsg.put("type", "CONNECT");
            connectMsg.put("system_key", SYSTEM_KEY);
            writer.println(objectMapper.writeValueAsString(connectMsg));

            LOGGER.info("RealtimeClient connected to server at " + HOST + ":" + PORT);
        }
    }

    private synchronized void closeConnection() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        socket = null;
        writer = null;
    }

    public void stop() {
        running = false;
        closeConnection();
    }
}
