package com.parkinglot.domain.policy;

import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.VehicleType;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Allocates slots by preferring the least occupied compatible level.
 */
public final class LevelBalancedStrategy implements SlotAllocationStrategy {
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ParkingSlot> allocate(final List<ParkingLevel> levels, final VehicleType vehicleType) {
        Objects.requireNonNull(levels, "levels must not be null");
        Objects.requireNonNull(vehicleType, "vehicleType must not be null");

        return levels.stream()
                .filter(level -> level.getAvailableSlots().stream()
                        .anyMatch(slot -> slot.getSlotType().supports(vehicleType)))
                .sorted(Comparator.comparingDouble(ParkingLevel::getOccupancyRate)
                        .thenComparingInt(ParkingLevel::getFloorNumber))
                .map(level -> level.getAvailableSlots().stream()
                        .filter(slot -> slot.getSlotType().supports(vehicleType))
                        .sorted(Comparator.comparingInt(slot -> extractNumericOrder(slot.getSlotNumber()))
                                .thenComparing(ParkingSlot::getSlotNumber))
                        .findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private int extractNumericOrder(final String slotNumber) {
        final String digits = slotNumber.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(digits);
    }
}
