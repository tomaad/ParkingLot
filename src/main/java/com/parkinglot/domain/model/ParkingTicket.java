package com.parkinglot.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a parking session ticket.
 */
public final class ParkingTicket {
    private final UUID ticketId;
    private final Vehicle vehicle;
    private final String slotId;
    private final String levelId;
    private final Instant entryTime;

    private Instant exitTime;
    private TicketStatus status;

    /**
     * Creates a parking ticket.
     *
     * @param ticketId ticket identifier
     * @param vehicle parked vehicle
     * @param slotId allocated slot identifier
     * @param levelId level identifier
     * @param entryTime entry timestamp
     * @param status ticket status
     */
    public ParkingTicket(final UUID ticketId,
                         final Vehicle vehicle,
                         final String slotId,
                         final String levelId,
                         final Instant entryTime,
                         final TicketStatus status) {
        this.ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle must not be null");
        this.slotId = requireText(slotId, "slotId");
        this.levelId = requireText(levelId, "levelId");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Returns the ticket identifier.
     *
     * @return ticket identifier
     */
    public UUID getTicketId() {
        return ticketId;
    }

    /**
     * Returns the vehicle.
     *
     * @return parked vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
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
     * Returns the level identifier.
     *
     * @return level identifier
     */
    public String getLevelId() {
        return levelId;
    }

    /**
     * Returns the entry time.
     *
     * @return entry timestamp
     */
    public Instant getEntryTime() {
        return entryTime;
    }

    /**
     * Returns the exit time when available.
     *
     * @return optional exit timestamp
     */
    public Optional<Instant> getExitTime() {
        return Optional.ofNullable(exitTime);
    }

    /**
     * Returns the ticket status.
     *
     * @return ticket status
     */
    public TicketStatus getStatus() {
        return status;
    }

    /**
     * Closes the ticket with an exit timestamp.
     *
     * @param exitTime exit timestamp
     */
    public void close(final Instant exitTime) {
        final Instant resolvedExitTime = Objects.requireNonNull(exitTime, "exitTime must not be null");
        if (status != TicketStatus.ACTIVE) {
            throw new IllegalStateException("Only active tickets can be closed");
        }
        if (resolvedExitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("exitTime cannot be before entryTime");
        }
        this.exitTime = resolvedExitTime;
        this.status = TicketStatus.COMPLETED;
    }

    /**
     * Returns the ticket duration.
     *
     * @return duration from entry until exit or now when active
     */
    public Duration getDuration() {
        final Instant effectiveExitTime = exitTime == null ? Instant.now() : exitTime;
        return Duration.between(entryTime, effectiveExitTime);
    }

    /**
     * Indicates whether the ticket is active.
     *
     * @return {@code true} when the ticket is active
     */
    public boolean isActive() {
        return status == TicketStatus.ACTIVE;
    }

    private static String requireText(final String value, final String fieldName) {
        final String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
