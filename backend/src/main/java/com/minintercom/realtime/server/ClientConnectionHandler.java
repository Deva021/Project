package com.minintercom.realtime.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minintercom.realtime.events.RealtimeEvent;
import com.minintercom.realtime.router.EventRouter;
import com.minintercom.realtime.router.Subscriber;
import com.minintercom.security.JwtService;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles communication with a single TCP client.
 */
public class ClientConnectionHandler implements Runnable, Subscriber {

    private static final Logger LOGGER = Logger.getLogger(ClientConnectionHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_KEY;

    static {
        java.util.Properties prop = new java.util.Properties();
        try (java.io.InputStream input = ClientConnectionHandler.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                prop.load(input);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        // Use EnvLoader to check environment or .env file
        SYSTEM_KEY = com.minintercom.common.EnvLoader.get("RELAY_SYSTEM_KEY");
    }

    private final Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private UUID tenantId;
    private final Set<UUID> subscribedConversations = new HashSet<>();
    private boolean authenticated = false;
    private boolean isSystem = false;

    public ClientConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while ((line = reader.readLine()) != null) {
                handleMessage(line);
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Client disconnected: " + socket.getRemoteSocketAddress());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String line) {
        try {
            JsonNode node = objectMapper.readTree(line);
            String type = node.get("type").asText();

            switch (type) {
                case "CONNECT":
                    handleConnect(node);
                    break;
                case "EVENT":
                    handleEvent(node);
                    break;
                case "DISCONNECT":
                    cleanup();
                    break;
                default:
                    sendError("INVALID_TYPE", "Unknown message type: " + type);
            }
        } catch (Exception e) {
            sendError("INVALID_FORMAT", "Failed to parse message: " + e.getMessage());
        }
    }

    private void handleConnect(JsonNode node) {
        if (node.has("system_key")) {
            String key = node.get("system_key").asText();
            if (SYSTEM_KEY.equals(key)) {
                this.authenticated = true;
                this.isSystem = true;
                LOGGER.info("System client authenticated");
                return;
            } else {
                sendError("UNAUTHORIZED", "Invalid system key");
                cleanup();
                return;
            }
        }

        if (!node.has("auth_token") || !node.has("tenant_id")) {
            sendError("BAD_REQUEST", "Missing auth_token or tenant_id");
            return;
        }

        String token = node.get("auth_token").asText();
        String tenantIdStr = node.get("tenant_id").asText();

        try {
            // Verify JWT and extract tenant ID
            DecodedJWT decodedJWT = JwtService.validateToken(token);
            if (decodedJWT != null) {
                String jwtTenantIdStr = JwtService.getClaim(decodedJWT, "tenant_id");

                if (jwtTenantIdStr == null || !jwtTenantIdStr.equals(tenantIdStr)) {
                    sendError("UNAUTHORIZED", "Token tenant_id mismatch");
                    cleanup();
                    return;
                }

                this.tenantId = UUID.fromString(tenantIdStr);
                this.authenticated = true;

                // Handle initial subscriptions
                if (node.has("subscriptions")) {
                    for (JsonNode sub : node.get("subscriptions")) {
                        UUID convId = UUID.fromString(sub.asText());
                        subscribe(convId);
                    }
                }

                LOGGER.info("Client authenticated for tenant: " + tenantId);
            } else {
                sendError("UNAUTHORIZED", "Invalid token");
                cleanup();
            }
        } catch (Exception e) {
            sendError("UNAUTHORIZED", "Authentication failed: " + e.getMessage());
            cleanup();
        }
    }

    private void handleEvent(JsonNode node) {
        if (!authenticated) {
            sendError("UNAUTHORIZED", "Not authenticated");
            return;
        }

        try {
            RealtimeEvent event = objectMapper.treeToValue(node, RealtimeEvent.class);

            // Strict tenant isolation for non-system clients
            if (!isSystem && !event.getTenantId().equals(this.tenantId)) {
                sendError("UNAUTHORIZED", "Tenant ID mismatch in event");
                return;
            }

            EventRouter.getInstance().route(event);
        } catch (Exception e) {
            sendError("INVALID_EVENT", "Failed to process event: " + e.getMessage());
        }
    }

    private void subscribe(UUID conversationId) {
        if (subscribedConversations.add(conversationId)) {
            EventRouter.getInstance().subscribe(tenantId, conversationId, this);
        }
    }

    private void sendError(String code, String message) {
        try {
            String errorJson = objectMapper.writeValueAsString(new ErrorResponse(code, message));
            writer.println(errorJson);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending error response", e);
        }
    }

    @Override
    public void onEvent(RealtimeEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            writer.println(json);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending event to client", e);
        }
    }

    @Override
    public UUID getTenantId() {
        return tenantId;
    }

    private void cleanup() {
        authenticated = false;
        if (tenantId != null) {
            for (UUID convId : subscribedConversations) {
                EventRouter.getInstance().unsubscribe(tenantId, convId, this);
            }
            EventRouter.getInstance().unsubscribeTenant(tenantId, this);
        }
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    private static class ErrorResponse {
        public String type = "ERROR";
        public String code;
        public String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
