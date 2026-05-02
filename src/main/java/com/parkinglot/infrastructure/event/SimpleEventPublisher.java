package com.parkinglot.infrastructure.event;

import com.parkinglot.application.event.DomainEventPublisher;
import com.parkinglot.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple synchronous event publisher backed by in-process listeners.
 */
public final class SimpleEventPublisher implements DomainEventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEventPublisher.class);

    private final List<Consumer<DomainEvent>> listeners = new CopyOnWriteArrayList<>();

    /**
     * Registers a listener for published events.
     *
     * @param listener listener callback
     */
    public void registerListener(final Consumer<DomainEvent> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(final DomainEvent event) {
        final DomainEvent domainEvent = Objects.requireNonNull(event, "event must not be null");
        LOGGER.info("Publishing domain event type={}, id={}, occurredAt={}",
                domainEvent.getEventType(),
                domainEvent.getEventId(),
                domainEvent.getOccurredAt());
        for (Consumer<DomainEvent> listener : listeners) {
            listener.accept(domainEvent);
        }
    }
}
