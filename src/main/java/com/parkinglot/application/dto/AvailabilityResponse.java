package com.parkinglot.application.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Response DTO describing current lot availability.
 */
public final class AvailabilityResponse {
    private final int totalSlots;
    private final int availableSlots;
    private final double occupancyRate;
    private final Map<Integer, Integer> levelAvailability;

    /**
     * Creates an availability response.
     *
     * @param totalSlots total slot count
     * @param availableSlots available slot count
     * @param occupancyRate occupancy rate from 0 to 1
     * @param levelAvailability per-level availability
     */
    public AvailabilityResponse(final int totalSlots,
                                final int availableSlots,
                                final double occupancyRate,
                                final Map<Integer, Integer> levelAvailability) {
        this.totalSlots = totalSlots;
        this.availableSlots = availableSlots;
        this.occupancyRate = occupancyRate;
        this.levelAvailability = Collections.unmodifiableMap(new LinkedHashMap<>(
                Objects.requireNonNull(levelAvailability, "levelAvailability must not be null")));
    }

    /**
     * Returns the total slot count.
     *
     * @return total slots
     */
    public int getTotalSlots() {
        return totalSlots;
    }

    /**
     * Returns the available slot count.
     *
     * @return available slots
     */
    public int getAvailableSlots() {
        return availableSlots;
    }

    /**
     * Returns the occupancy rate.
     *
     * @return occupancy rate
     */
    public double getOccupancyRate() {
        return occupancyRate;
    }

    /**
     * Returns per-level availability.
     *
     * @return availability keyed by level number
     */
    public Map<Integer, Integer> getLevelAvailability() {
        return levelAvailability;
    }
}
