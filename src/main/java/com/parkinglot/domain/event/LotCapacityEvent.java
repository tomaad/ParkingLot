package com.parkinglot.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Raised when lot occupancy exceeds a configured threshold.
 */
public final class LotCapacityEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredAt;
    private final String lotId;
    private final int currentOccupancy;
    private final int totalCapacity;

    /**
     * Creates a lot capacity event.
     *
     * @param lotId lot identifier
     * @param currentOccupancy current occupied slot count
     * @param totalCapacity total slot capacity
     * @param occurredAt event timestamp
     */
    public LotCapacityEvent(final String lotId,
                            final int currentOccupancy,
                            final int totalCapacity,
                            final Instant occurredAt) {
        this.eventId = UUID.randomUUID();
        this.lotId = requireText(lotId, "lotId");
        this.currentOccupancy = currentOccupancy;
        this.totalCapacity = totalCapacity;
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
        return "LotCapacityEvent";
    }

    /**
     * Returns the lot identifier.
     *
     * @return lot identifier
     */
    public String getLotId() {
        return lotId;
    }

    /**
     * Returns the current occupancy.
     *
     * @return occupied slot count
     */
    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    /**
     * Returns the total capacity.
     *
     * @return total capacity
     */
    public int getTotalCapacity() {
        return totalCapacity;
    }

    private static String requireText(final String value, final String fieldName) {
        final String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
