package com.parkinglot.domain.policy;

import com.parkinglot.domain.model.LicensePlate;
import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.SlotStatus;
import com.parkinglot.domain.model.SlotType;
import com.parkinglot.domain.model.Vehicle;
import com.parkinglot.domain.model.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NearestSlotStrategy")
class NearestSlotStrategyTest {

    private final NearestSlotStrategy strategy = new NearestSlotStrategy();

    @Test
    @DisplayName("allocates the lowest-numbered slot on the lowest floor")
    void allocatesLowestNumberedSlotOnLowestFloor() {
        ParkingLevel levelTwo = new ParkingLevel("level-2", 2, List.of(
                createSlot("slot-3", "B10", SlotType.REGULAR, "level-2"),
                createSlot("slot-4", "B2", SlotType.REGULAR, "level-2")));
        ParkingLevel levelOne = new ParkingLevel("level-1", 1, List.of(
                createSlot("slot-1", "A10", SlotType.REGULAR, "level-1"),
                createSlot("slot-2", "A2", SlotType.REGULAR, "level-1")));

        ParkingSlot slot = strategy.allocate(List.of(levelTwo, levelOne), VehicleType.CAR).orElseThrow();

        assertEquals("slot-2", slot.getSlotId());
    }

    @Test
    @DisplayName("skips occupied slots")
    void skipsOccupiedSlots() {
        ParkingSlot occupied = createSlot("slot-1", "A1", SlotType.REGULAR, "level-1");
        occupied.occupy(createVehicle("CAR-111", VehicleType.CAR));
        ParkingSlot available = createSlot("slot-2", "A2", SlotType.REGULAR, "level-1");
        ParkingLevel level = new ParkingLevel("level-1", 1, List.of(occupied, available));

        ParkingSlot slot = strategy.allocate(List.of(level), VehicleType.CAR).orElseThrow();

        assertEquals("slot-2", slot.getSlotId());
    }

    @Test
    @DisplayName("returns empty when no compatible slot is available")
    void returnsEmptyWhenNoCompatibleSlotAvailable() {
        ParkingSlot occupiedLarge = createSlot("slot-1", "A1", SlotType.LARGE, "level-1");
        occupiedLarge.occupy(createVehicle("TRK-100", VehicleType.TRUCK));
        ParkingLevel level = new ParkingLevel("level-1", 1, List.of(
                occupiedLarge,
                createSlot("slot-2", "A2", SlotType.MOTORCYCLE, "level-1")));

        assertTrue(strategy.allocate(List.of(level), VehicleType.TRUCK).isEmpty());
    }

    @Test
    @DisplayName("respects vehicle type compatibility")
    void respectsVehicleTypeCompatibility() {
        ParkingLevel levelOne = new ParkingLevel("level-1", 1, List.of(
                createSlot("slot-1", "A1", SlotType.MOTORCYCLE, "level-1")));
        ParkingLevel levelTwo = new ParkingLevel("level-2", 2, List.of(
                createSlot("slot-2", "B1", SlotType.REGULAR, "level-2")));

        ParkingSlot slot = strategy.allocate(List.of(levelOne, levelTwo), VehicleType.CAR).orElseThrow();

        assertEquals("slot-2", slot.getSlotId());
    }

    private ParkingSlot createSlot(String slotId, String slotNumber, SlotType slotType, String levelId) {
        return new ParkingSlot(slotId, slotNumber, slotType, SlotStatus.AVAILABLE, levelId);
    }

    private Vehicle createVehicle(String plate, VehicleType vehicleType) {
        return new Vehicle(new LicensePlate(plate), vehicleType, 40);
    }
}
