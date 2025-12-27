package com.minintercom.realtime.router;

import com.minintercom.realtime.events.RealtimeEvent;
import java.util.UUID;

/**
 * Interface for components that wish to receive real-time events.
 */
public interface Subscriber {
    /**
     * Called when a new event is routed to this subscriber.
     * 
     * @param event The event.
     */
    void onEvent(RealtimeEvent event);

    /**
     * @return The tenant ID this subscriber belongs to.
     */
    UUID getTenantId();
}
