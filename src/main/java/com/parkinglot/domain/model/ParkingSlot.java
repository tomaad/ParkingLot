package com.parkinglot.domain.model;

import com.parkinglot.domain.exception.InvalidSlotTransitionException;
import com.parkinglot.domain.exception.SlotNotOccupiedException;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an individual parking slot with guarded state transitions.
 */
public final class ParkingSlot {
    private final String slotId;
    private final String slotNumber;
    private final SlotType slotType;
    private final String levelId;

    private SlotStatus status;
    private Vehicle currentVehicle;

    /**
     * Creates a parking slot.
     *
     * @param slotId slot identifier
     * @param slotNumber human-readable slot number
     * @param slotType slot category
     * @param status initial slot status
     * @param levelId owning level identifier
     */
    public ParkingSlot(final String slotId,
                       final String slotNumber,
                       final SlotType slotType,
                       final SlotStatus status,
                       final String levelId) {
        this.slotId = requireText(slotId, "slotId");
        this.slotNumber = requireText(slotNumber, "slotNumber");
        this.slotType = Objects.requireNonNull(slotType, "slotType must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.levelId = requireText(levelId, "levelId");
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
     * Returns the slot number.
     *
     * @return slot number
     */
    public String getSlotNumber() {
        return slotNumber;
    }

    /**
     * Returns the slot type.
     *
     * @return slot type
     */
    public SlotType getSlotType() {
        return slotType;
    }

    /**
     * Returns the slot status.
     *
     * @return slot status
     */
    public SlotStatus getStatus() {
        return status;
    }

    /**
     * Returns the owning level identifier.
     *
     * @return level identifier
     */
    public String getLevelId() {
        return levelId;
    }

    /**
     * Returns the currently parked vehicle, if any.
     *
     * @return optional current vehicle
     */
    public Optional<Vehicle> getCurrentVehicle() {
        return Optional.ofNullable(currentVehicle);
    }

    /**
     * Occupies the slot with the given vehicle.
     *
     * @param vehicle vehicle to place in the slot
     */
    public void occupy(final Vehicle vehicle) {
        final Vehicle incomingVehicle = Objects.requireNonNull(vehicle, "vehicle must not be null");
        if (status != SlotStatus.AVAILABLE && status != SlotStatus.RESERVED) {
            throw new InvalidSlotTransitionException(
                    String.format("Cannot occupy slot %s while status is %s", slotId, status));
        }
        if (!slotType.supports(incomingVehicle.getVehicleType())) {
            throw new InvalidSlotTransitionException(
                    String.format("Slot %s of type %s does not support %s", slotId, slotType,
                            incomingVehicle.getVehicleType()));
        }
        this.currentVehicle = incomingVehicle;
        this.status = SlotStatus.OCCUPIED;
    }

    /**
     * Vacates the slot.
     */
    public void vacate() {
        if (status != SlotStatus.OCCUPIED) {
            throw new SlotNotOccupiedException(String.format("Slot %s is not occupied", slotId));
        }
        this.currentVehicle = null;
        this.status = SlotStatus.AVAILABLE;
    }

    /**
     * Reserves the slot for later use.
     */
    public void reserve() {
        if (status != SlotStatus.AVAILABLE) {
            throw new InvalidSlotTransitionException(
                    String.format("Cannot reserve slot %s while status is %s", slotId, status));
        }
        this.status = SlotStatus.RESERVED;
    }

    /**
     * Marks the slot as unavailable for service.
     */
    public void markOutOfService() {
        if (status == SlotStatus.OCCUPIED) {
            throw new InvalidSlotTransitionException(
                    String.format("Cannot mark occupied slot %s out of service", slotId));
        }
        if (status == SlotStatus.OUT_OF_SERVICE) {
            throw new InvalidSlotTransitionException(
                    String.format("Slot %s is already out of service", slotId));
        }
        this.currentVehicle = null;
        this.status = SlotStatus.OUT_OF_SERVICE;
    }

    private static String requireText(final String value, final String fieldName) {
        final String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
