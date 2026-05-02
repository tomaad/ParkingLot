package com.parkinglot.application.dto;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Response DTO for a successful unpark operation.
 */
public final class UnparkResponse {
    private final String ticketId;
    private final Duration duration;
    private final Instant exitTime;

    /**
     * Creates an unpark response.
     *
     * @param ticketId ticket identifier
     * @param duration parking duration
     * @param exitTime exit timestamp
     */
    public UnparkResponse(final String ticketId, final Duration duration, final Instant exitTime) {
        this.ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        this.duration = Objects.requireNonNull(duration, "duration must not be null");
        this.exitTime = Objects.requireNonNull(exitTime, "exitTime must not be null");
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
     * Returns the parking duration.
     *
     * @return parking duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Returns the exit timestamp.
     *
     * @return exit timestamp
     */
    public Instant getExitTime() {
        return exitTime;
    }
}
