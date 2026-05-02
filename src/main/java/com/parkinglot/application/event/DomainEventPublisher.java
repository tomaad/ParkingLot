package com.parkinglot.application.event;

import com.parkinglot.domain.event.DomainEvent;

/**
 * Publishes domain events to interested infrastructure listeners.
 */
public interface DomainEventPublisher {
    /**
     * Publishes the supplied event.
     *
     * @param event event to publish
     */
    void publish(DomainEvent event);
}
