package com.parkinglot.domain.model;

import com.parkinglot.domain.policy.SlotAllocationStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root representing a multi-level parking lot.
 */
public final class ParkingLot {
    private final String lotId;
    private final String name;
    private final String address;
    private final List<ParkingLevel> levels;

    /**
     * Creates a parking lot aggregate.
     *
     * @param lotId lot identifier
     * @param name lot name
     * @param address street address
     * @param levels parking levels
     */
    public ParkingLot(final String lotId,
                      final String name,
                      final String address,
                      final List<ParkingLevel> levels) {
        this.lotId = requireText(lotId, "lotId");
        this.name = requireText(name, "name");
        this.address = requireText(address, "address");
        this.levels = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(levels, "levels must not be null")));
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
     * Returns the lot name.
     *
     * @return lot name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the lot address.
     *
     * @return lot address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the parking levels.
     *
     * @return immutable level list
     */
    public List<ParkingLevel> getLevels() {
        return levels;
    }

    /**
     * Returns the number of available slots across the lot.
     *
     * @return available slot count
     */
    public int getAvailableSlotCount() {
        return levels.stream().mapToInt(level -> level.getAvailableSlots().size()).sum();
    }

    /**
     * Finds an available slot using the supplied strategy.
     *
     * @param vehicleType requested vehicle type
     * @param allocationStrategy slot allocation strategy
     * @return optional matching slot
     */
    public Optional<ParkingSlot> findAvailableSlot(final VehicleType vehicleType,
                                                   final SlotAllocationStrategy allocationStrategy) {
        Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        Objects.requireNonNull(allocationStrategy, "allocationStrategy must not be null");
        return allocationStrategy.allocate(levels, vehicleType);
    }

    /**
     * Returns the total capacity of the lot.
     *
     * @return total slot capacity
     */
    public int getTotalCapacity() {
        return levels.stream().mapToInt(level -> level.getSlots().size()).sum();
    }

    /**
     * Finds a level by identifier.
     *
     * @param levelId level identifier
     * @return matching level if present
     */
    public Optional<ParkingLevel> findLevelById(final String levelId) {
        final String normalized = requireText(levelId, "levelId");
        return levels.stream().filter(level -> level.getLevelId().equals(normalized)).findFirst();
    }

    /**
     * Finds a slot by identifier.
     *
     * @param slotId slot identifier
     * @return matching slot if present
     */
    public Optional<ParkingSlot> findSlotById(final String slotId) {
        final String normalized = requireText(slotId, "slotId");
        return levels.stream()
                .map(level -> level.findSlotById(normalized))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private static String requireText(final String value, final String fieldName) {
        final String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
