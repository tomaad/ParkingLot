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

@DisplayName("LevelBalancedStrategy")
class LevelBalancedStrategyTest {

    private final LevelBalancedStrategy strategy = new LevelBalancedStrategy();

    @Test
    @DisplayName("distributes to the level with the most available slots")
    void distributesToLevelWithMostAvailableSlots() {
        ParkingSlot busySlot = createSlot("slot-1", "A1", SlotType.REGULAR, "level-1");
        busySlot.occupy(createVehicle("CAR-100", VehicleType.CAR));
        ParkingLevel levelOne = new ParkingLevel("level-1", 1, List.of(
                busySlot,
                createSlot("slot-2", "A2", SlotType.REGULAR, "level-1"),
                createSlot("slot-3", "A3", SlotType.REGULAR, "level-1")));
        ParkingLevel levelTwo = new ParkingLevel("level-2", 2, List.of(
                createSlot("slot-4", "B1", SlotType.REGULAR, "level-2"),
                createSlot("slot-5", "B2", SlotType.REGULAR, "level-2"),
                createSlot("slot-6", "B3", SlotType.REGULAR, "level-2")));

        ParkingSlot slot = strategy.allocate(List.of(levelOne, levelTwo), VehicleType.CAR).orElseThrow();

        assertEquals("slot-4", slot.getSlotId());
    }

    @Test
    @DisplayName("returns empty when all levels are full")
    void returnsEmptyWhenAllFull() {
        ParkingSlot slot = createSlot("slot-1", "A1", SlotType.REGULAR, "level-1");
        slot.occupy(createVehicle("CAR-200", VehicleType.CAR));
        ParkingLevel level = new ParkingLevel("level-1", 1, List.of(slot));

        assertTrue(strategy.allocate(List.of(level), VehicleType.CAR).isEmpty());
    }

    private ParkingSlot createSlot(String slotId, String slotNumber, SlotType slotType, String levelId) {
        return new ParkingSlot(slotId, slotNumber, slotType, SlotStatus.AVAILABLE, levelId);
    }

    private Vehicle createVehicle(String plate, VehicleType vehicleType) {
        return new Vehicle(new LicensePlate(plate), vehicleType, 33);
    }
}
