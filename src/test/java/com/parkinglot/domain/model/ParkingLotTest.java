package com.parkinglot.domain.model;

import com.parkinglot.domain.policy.NearestSlotStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ParkingLot")
class ParkingLotTest {

    @Nested
    @DisplayName("capacity and availability")
    class CapacityAndAvailabilityTests {

        @Test
        @DisplayName("returns all slots as available when empty")
        void getAvailableSlotCountReturnsAllSlotsWhenEmpty() {
            ParkingLot parkingLot = createParkingLot();

            assertEquals(4, parkingLot.getAvailableSlotCount());
        }

        @Test
        @DisplayName("decreases available slot count after parking")
        void getAvailableSlotCountDecreasesAfterParking() {
            ParkingLot parkingLot = createParkingLot();
            ParkingSlot slot = parkingLot.findSlotById("slot-1").orElseThrow();

            slot.occupy(createVehicle("CAR-111", VehicleType.CAR));

            assertEquals(3, parkingLot.getAvailableSlotCount());
        }

        @Test
        @DisplayName("returns total capacity across all levels")
        void getTotalCapacityReturnsSumOfAllLevelSlots() {
            ParkingLot parkingLot = createParkingLot();

            assertEquals(4, parkingLot.getTotalCapacity());
        }
    }

    @Nested
    @DisplayName("lookup")
    class LookupTests {

        @Test
        @DisplayName("finds available slot when one exists")
        void findAvailableSlotReturnsSlotWhenAvailable() {
            ParkingLot parkingLot = createParkingLot();

            ParkingSlot slot = parkingLot.findAvailableSlot(VehicleType.CAR, new NearestSlotStrategy()).orElseThrow();

            assertEquals("slot-1", slot.getSlotId());
        }

        @Test
        @DisplayName("returns empty when lot is full")
        void findAvailableSlotReturnsEmptyWhenLotIsFull() {
            ParkingLot parkingLot = createParkingLot();
            parkingLot.getLevels().forEach(level -> level.getSlots().forEach(slot ->
                    slot.occupy(createVehicle("CAR-" + slot.getSlotNumber(), VehicleType.CAR))));

            assertTrue(parkingLot.findAvailableSlot(VehicleType.CAR, new NearestSlotStrategy()).isEmpty());
        }

        @Test
        @DisplayName("finds level by id when present")
        void findLevelByIdReturnsLevelWhenExists() {
            ParkingLot parkingLot = createParkingLot();

            assertEquals(2, parkingLot.findLevelById("level-2").orElseThrow().getFloorNumber());
        }

        @Test
        @DisplayName("finds slot by id across levels")
        void findSlotByIdSearchesAcrossLevels() {
            ParkingLot parkingLot = createParkingLot();

            assertEquals("D1", parkingLot.findSlotById("slot-4").orElseThrow().getSlotNumber());
        }
    }

    private ParkingLot createParkingLot() {
        ParkingLevel levelOne = new ParkingLevel("level-1", 1, List.of(
                new ParkingSlot("slot-1", "A1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1"),
                new ParkingSlot("slot-2", "B1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1")));
        ParkingLevel levelTwo = new ParkingLevel("level-2", 2, List.of(
                new ParkingSlot("slot-3", "C1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-2"),
                new ParkingSlot("slot-4", "D1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-2")));
        return new ParkingLot("lot-1", "Downtown", "123 Main St", List.of(levelOne, levelTwo));
    }

    private Vehicle createVehicle(String plate, VehicleType vehicleType) {
        return new Vehicle(new LicensePlate(plate), vehicleType, 28);
    }
}
