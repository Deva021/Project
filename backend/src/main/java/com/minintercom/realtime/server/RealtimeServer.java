package com.minintercom.realtime.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TCP server that listens for real-time client connections.
 */
public class RealtimeServer {

    private static final Logger LOGGER = Logger.getLogger(RealtimeServer.class.getName());
    private static final int PORT = 9090; // Default port
    private static final int THREAD_POOL_SIZE = 100;

    private final int port;
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private boolean running = false;

    public RealtimeServer() {
        this(PORT);
    }

    public RealtimeServer(int port) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Starts the server in a new thread.
     */
    public void start() {
        if (running)
            return;
        running = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                LOGGER.info("RealtimeServer started on port " + port);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        executorService.execute(new ClientConnectionHandler(clientSocket));
                    } catch (IOException e) {
                        if (running) {
                            LOGGER.log(Level.SEVERE, "Error accepting client connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not start RealtimeServer on port " + port, e);
            } finally {
                stop();
            }
        }).start();
    }

    /**
     * Stops the server and shuts down the executor service.
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error closing ServerSocket", e);
        }
        executorService.shutdown();
        LOGGER.info("RealtimeServer stopped");
    }
}
