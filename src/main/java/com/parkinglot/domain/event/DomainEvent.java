package com.parkinglot.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base contract for domain events.
 */
public interface DomainEvent {
    /**
     * Returns the event identifier.
     *
     * @return event identifier
     */
    UUID getEventId();

    /**
     * Returns the event timestamp.
     *
     * @return occurrence timestamp
     */
    Instant getOccurredAt();

    /**
     * Returns the event type name.
     *
     * @return event type
     */
    String getEventType();
}
