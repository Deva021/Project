package com.minintercom.realtime.pollqueue;

import com.minintercom.realtime.events.RealtimeEvent;
import com.minintercom.realtime.router.EventRouter;
import com.minintercom.realtime.router.Subscriber;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing long-polling event queues.
 */
public class PollQueueService {

    private static final PollQueueService INSTANCE = new PollQueueService();

    private PollQueueService() {
    }

    public static PollQueueService getInstance() {
        return INSTANCE;
    }

    /**
     * Polls for events for a specific conversation.
     * Blocks until an event is available or the timeout is reached.
     */
    public List<RealtimeEvent> poll(UUID tenantId, UUID conversationId, long timeoutMs) {
        PollSubscriber subscriber = new PollSubscriber(tenantId);
        EventRouter.getInstance().subscribe(tenantId, conversationId, subscriber);

        try {
            RealtimeEvent event = subscriber.queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            List<RealtimeEvent> events = new ArrayList<>();
            if (event != null) {
                events.add(event);
                // Drain any other events that might have arrived
                subscriber.queue.drainTo(events);
            }
            return events;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } finally {
            EventRouter.getInstance().unsubscribe(tenantId, conversationId, subscriber);
        }
    }

    /**
     * Polls for all events within a tenant.
     */
    public List<RealtimeEvent> pollTenant(UUID tenantId, long timeoutMs) {
        PollSubscriber subscriber = new PollSubscriber(tenantId);
        EventRouter.getInstance().subscribeTenant(tenantId, subscriber);

        try {
            RealtimeEvent event = subscriber.queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            List<RealtimeEvent> events = new ArrayList<>();
            if (event != null) {
                events.add(event);
                subscriber.queue.drainTo(events);
            }
            return events;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } finally {
            EventRouter.getInstance().unsubscribeTenant(tenantId, subscriber);
        }
    }

    private static class PollSubscriber implements Subscriber {
        private final BlockingQueue<RealtimeEvent> queue = new LinkedBlockingQueue<>();
        private final UUID tenantId;

        public PollSubscriber(UUID tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        public void onEvent(RealtimeEvent event) {
            queue.offer(event);
        }

        @Override
        public UUID getTenantId() {
            return tenantId;
        }
    }
}
