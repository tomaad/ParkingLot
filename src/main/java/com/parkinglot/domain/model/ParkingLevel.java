package com.parkinglot.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a single level in the parking structure.
 */
public final class ParkingLevel {
    private final String levelId;
    private final int floorNumber;
    private final List<ParkingSlot> slots;

    /**
     * Creates a parking level.
     *
     * @param levelId level identifier
     * @param floorNumber floor number
     * @param slots slots present on the level
     */
    public ParkingLevel(final String levelId, final int floorNumber, final List<ParkingSlot> slots) {
        this.levelId = requireText(levelId, "levelId");
        this.floorNumber = floorNumber;
        this.slots = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(slots, "slots must not be null")));
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
     * Returns the floor number.
     *
     * @return floor number
     */
    public int getFloorNumber() {
        return floorNumber;
    }

    /**
     * Returns all slots on the level.
     *
     * @return immutable slot list
     */
    public List<ParkingSlot> getSlots() {
        return slots;
    }

    /**
     * Returns all currently available slots.
     *
     * @return available slots
     */
    public List<ParkingSlot> getAvailableSlots() {
        return slots.stream()
                .filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    /**
     * Returns available slots for a specific slot type.
     *
     * @param slotType slot type to filter by
     * @return matching available slots
     */
    public List<ParkingSlot> getAvailableSlotsByType(final SlotType slotType) {
        Objects.requireNonNull(slotType, "slotType must not be null");
        return slots.stream()
                .filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE)
                .filter(slot -> slot.getSlotType() == slotType)
                .collect(Collectors.toList());
    }

    /**
     * Returns the occupancy rate for the level.
     *
     * @return occupancy rate between 0 and 1
     */
    public double getOccupancyRate() {
        if (slots.isEmpty()) {
            return 0.0d;
        }
        final long occupiedCount = slots.stream()
                .filter(slot -> slot.getStatus() == SlotStatus.OCCUPIED)
                .count();
        return occupiedCount / (double) slots.size();
    }

    /**
     * Finds a slot by identifier.
     *
     * @param slotId slot identifier
     * @return matching slot if present
     */
    public Optional<ParkingSlot> findSlotById(final String slotId) {
        final String normalized = requireText(slotId, "slotId");
        return slots.stream().filter(slot -> slot.getSlotId().equals(normalized)).findFirst();
    }

    private static String requireText(final String value, final String fieldName) {
        final String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
