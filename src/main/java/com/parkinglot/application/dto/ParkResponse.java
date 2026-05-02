package com.parkinglot.application.dto;

import java.time.Instant;
import java.util.Objects;

/**
 * Response DTO for a successful parking operation.
 */
public final class ParkResponse {
    private final String ticketId;
    private final String slotNumber;
    private final int levelNumber;
    private final Instant entryTime;

    /**
     * Creates a park response.
     *
     * @param ticketId ticket identifier
     * @param slotNumber allocated slot number
     * @param levelNumber allocated level number
     * @param entryTime entry timestamp
     */
    public ParkResponse(final String ticketId,
                        final String slotNumber,
                        final int levelNumber,
                        final Instant entryTime) {
        this.ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        this.slotNumber = Objects.requireNonNull(slotNumber, "slotNumber must not be null");
        this.levelNumber = levelNumber;
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime must not be null");
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
     * Returns the slot number.
     *
     * @return slot number
     */
    public String getSlotNumber() {
        return slotNumber;
    }

    /**
     * Returns the level number.
     *
     * @return level number
     */
    public int getLevelNumber() {
        return levelNumber;
    }

    /**
     * Returns the entry time.
     *
     * @return entry time
     */
    public Instant getEntryTime() {
        return entryTime;
    }
}
