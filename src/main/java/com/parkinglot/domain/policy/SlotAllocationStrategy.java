package com.parkinglot.domain.policy;

import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.VehicleType;

import java.util.List;
import java.util.Optional;

/**
 * Strategy interface for parking slot allocation.
 */
public interface SlotAllocationStrategy {
    /**
     * Allocates an appropriate slot for the supplied vehicle type.
     *
     * @param levels available parking levels
     * @param vehicleType vehicle type to allocate for
     * @return allocated slot when present
     */
    Optional<ParkingSlot> allocate(List<ParkingLevel> levels, VehicleType vehicleType);
}
