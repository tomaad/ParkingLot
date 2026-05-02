package com.parkinglot.domain.event;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Raised when a vehicle leaves the parking lot.
 */
public final class VehicleUnparkedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final String ticketId;
    private final String vehicleId;
    private final String slotId;
    private final Duration duration;

    /**
     * Creates an unparked event.
     *
     * @param ticketId ticket identifier
     * @param vehicleId vehicle identifier
     * @param slotId slot identifier
     * @param duration stay duration
     * @param occurredAt event timestamp
     */
    public VehicleUnparkedEvent(final String ticketId,
                                final String vehicleId,
                                final String slotId,
                                final Duration duration,
                                final Instant occurredAt) {
        this.eventId = UUID.randomUUID();
        this.ticketId = requireText(ticketId, "ticketId");
        this.vehicleId = requireText(vehicleId, "vehicleId");
        this.slotId = requireText(slotId, "slotId");
        this.duration = Objects.requireNonNull(duration, "duration must not be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getEventId() {
        return eventId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEventType() {
        return "VehicleUnparkedEvent";
    }

    /**
     * Returns the ticket identifier.
     *
     * @return ticket identifier
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * Returns the vehicle identifier.
     *
     * @return vehicle identifier
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Returns the slot identifier.
     *
     * @return slot identifier
     */
    public String getSlotId() {
        return slotId;
    }

    /**
     * Returns the parking duration.
     *
     * @return parking duration
     */
    public Duration getDuration() {
        return duration;
    }

    private static String requireText(final String value, final String fieldName) {
        final String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
