package com.minintercom.realtime.router;

import com.minintercom.realtime.events.RealtimeEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Central router for real-time events.
 * Manages subscribers and routes events based on tenant and conversation IDs.
 */
public class EventRouter {

    private static final EventRouter INSTANCE = new EventRouter();

    // tenantId -> conversationId -> Set<Subscriber>
    private final Map<UUID, Map<UUID, Set<Subscriber>>> conversationSubscribers = new ConcurrentHashMap<>();

    // tenantId -> Set<Subscriber> (for tenant-wide events like new conversations)
    private final Map<UUID, Set<Subscriber>> tenantSubscribers = new ConcurrentHashMap<>();

    private EventRouter() {
    }

    public static EventRouter getInstance() {
        return INSTANCE;
    }

    /**
     * Subscribes a subscriber to a specific conversation.
     */
    public void subscribe(UUID tenantId, UUID conversationId, Subscriber subscriber) {
        conversationSubscribers
                .computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(conversationId, k -> new CopyOnWriteArraySet<>())
                .add(subscriber);
    }

    /**
     * Subscribes a subscriber to all events within a tenant.
     */
    public void subscribeTenant(UUID tenantId, Subscriber subscriber) {
        tenantSubscribers
                .computeIfAbsent(tenantId, k -> new CopyOnWriteArraySet<>())
                .add(subscriber);
    }

    /**
     * Unsubscribes a subscriber from a specific conversation.
     */
    public void unsubscribe(UUID tenantId, UUID conversationId, Subscriber subscriber) {
        Map<UUID, Set<Subscriber>> tenantMap = conversationSubscribers.get(tenantId);
        if (tenantMap != null) {
            Set<Subscriber> subscribers = tenantMap.get(conversationId);
            if (subscribers != null) {
                subscribers.remove(subscriber);
            }
        }
    }

    /**
     * Unsubscribes a subscriber from all tenant events.
     */
    public void unsubscribeTenant(UUID tenantId, Subscriber subscriber) {
        Set<Subscriber> subscribers = tenantSubscribers.get(tenantId);
        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
    }

    /**
     * Routes an event to all relevant subscribers.
     */
    public void route(RealtimeEvent event) {
        UUID tenantId = event.getTenantId();
        UUID conversationId = event.getConversationId();

        // Route to tenant-wide subscribers
        Set<Subscriber> tSubscribers = tenantSubscribers.get(tenantId);
        if (tSubscribers != null) {
            tSubscribers.forEach(s -> s.onEvent(event));
        }

        // Route to conversation-specific subscribers
        if (conversationId != null) {
            Map<UUID, Set<Subscriber>> tenantMap = conversationSubscribers.get(tenantId);
            if (tenantMap != null) {
                Set<Subscriber> cSubscribers = tenantMap.get(conversationId);
                if (cSubscribers != null) {
                    cSubscribers.forEach(s -> s.onEvent(event));
                }
            }
        }
    }
}
