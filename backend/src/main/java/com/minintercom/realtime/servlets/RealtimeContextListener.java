package com.minintercom.realtime.servlets;

import com.minintercom.realtime.server.RealtimeServer;
import com.minintercom.realtime.client.RealtimeClient;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Manages the lifecycle of the RealtimeServer.
 */
@WebListener
public class RealtimeContextListener implements ServletContextListener {

    private RealtimeServer realtimeServer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        realtimeServer = new RealtimeServer();
        realtimeServer.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (realtimeServer != null) {
            realtimeServer.stop();
        }
        RealtimeClient.getInstance().stop();
    }
}
