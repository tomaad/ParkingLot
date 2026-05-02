package com.parkinglot.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Request DTO for unparking a vehicle.
 */
public final class UnparkRequest {
    private final String ticketId;

    /**
     * Creates an unpark request.
     *
     * @param ticketId ticket identifier
     */
    @JsonCreator
    public UnparkRequest(@JsonProperty("ticketId") final String ticketId) {
        this.ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
    }

    /**
     * Returns the ticket identifier.
     *
     * @return ticket identifier
     */
    public String getTicketId() {
        return ticketId;
    }
}
