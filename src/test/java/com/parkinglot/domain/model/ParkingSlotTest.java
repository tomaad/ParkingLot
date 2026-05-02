package com.parkinglot.domain.model;

import com.parkinglot.domain.exception.InvalidSlotTransitionException;
import com.parkinglot.domain.exception.SlotNotOccupiedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ParkingSlot")
class ParkingSlotTest {

    @Nested
    @DisplayName("occupy")
    class OccupyTests {

        @Test
        @DisplayName("transitions from AVAILABLE to OCCUPIED")
        void occupyFromAvailableTransitionsToOccupied() {
            ParkingSlot slot = createSlot(SlotStatus.AVAILABLE, SlotType.REGULAR);
            Vehicle vehicle = createVehicle(VehicleType.CAR, "CAR-101");

            slot.occupy(vehicle);

            assertEquals(SlotStatus.OCCUPIED, slot.getStatus());
            assertTrue(slot.getCurrentVehicle().isPresent());
            assertEquals(vehicle, slot.getCurrentVehicle().orElseThrow());
        }

        @Test
        @DisplayName("transitions from RESERVED to OCCUPIED")
        void occupyFromReservedTransitionsToOccupied() {
            ParkingSlot slot = createSlot(SlotStatus.RESERVED, SlotType.REGULAR);
            Vehicle vehicle = createVehicle(VehicleType.CAR, "CAR-102");

            slot.occupy(vehicle);

            assertEquals(SlotStatus.OCCUPIED, slot.getStatus());
            assertEquals(vehicle, slot.getCurrentVehicle().orElseThrow());
        }

        @Test
        @DisplayName("rejects transition from OCCUPIED")
        void occupyFromOccupiedThrows() {
            ParkingSlot slot = createOccupiedSlot();

            assertThrows(InvalidSlotTransitionException.class,
                    () -> slot.occupy(createVehicle(VehicleType.CAR, "CAR-103")));
        }

        @Test
        @DisplayName("rejects transition from OUT_OF_SERVICE")
        void occupyFromOutOfServiceThrows() {
            ParkingSlot slot = createSlot(SlotStatus.OUT_OF_SERVICE, SlotType.REGULAR);

            assertThrows(InvalidSlotTransitionException.class,
                    () -> slot.occupy(createVehicle(VehicleType.CAR, "CAR-104")));
        }

        @Test
        @DisplayName("rejects incompatible vehicle type")
        void occupyWithIncompatibleVehicleTypeThrows() {
            ParkingSlot slot = createSlot(SlotStatus.AVAILABLE, SlotType.COMPACT);

            assertThrows(InvalidSlotTransitionException.class,
                    () -> slot.occupy(createVehicle(VehicleType.TRUCK, "TRK-200")));
        }
    }

    @Nested
    @DisplayName("vacate")
    class VacateTests {

        @Test
        @DisplayName("transitions from OCCUPIED to AVAILABLE")
        void vacateFromOccupiedTransitionsToAvailable() {
            ParkingSlot slot = createOccupiedSlot();

            slot.vacate();

            assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
            assertTrue(slot.getCurrentVehicle().isEmpty());
        }

        @Test
        @DisplayName("rejects transition from AVAILABLE")
        void vacateFromAvailableThrows() {
            ParkingSlot slot = createSlot(SlotStatus.AVAILABLE, SlotType.REGULAR);

            assertThrows(SlotNotOccupiedException.class, slot::vacate);
        }
    }

    @Nested
    @DisplayName("reserve")
    class ReserveTests {

        @Test
        @DisplayName("transitions from AVAILABLE to RESERVED")
        void reserveFromAvailableTransitionsToReserved() {
            ParkingSlot slot = createSlot(SlotStatus.AVAILABLE, SlotType.REGULAR);

            slot.reserve();

            assertEquals(SlotStatus.RESERVED, slot.getStatus());
        }

        @Test
        @DisplayName("rejects transition from OCCUPIED")
        void reserveFromOccupiedThrows() {
            ParkingSlot slot = createOccupiedSlot();

            assertThrows(InvalidSlotTransitionException.class, slot::reserve);
        }
    }

    @Nested
    @DisplayName("markOutOfService")
    class MarkOutOfServiceTests {

        @Test
        @DisplayName("transitions from AVAILABLE to OUT_OF_SERVICE")
        void markOutOfServiceFromAvailableTransitions() {
            ParkingSlot slot = createSlot(SlotStatus.AVAILABLE, SlotType.REGULAR);

            slot.markOutOfService();

            assertEquals(SlotStatus.OUT_OF_SERVICE, slot.getStatus());
            assertTrue(slot.getCurrentVehicle().isEmpty());
        }

        @Test
        @DisplayName("rejects transition from OCCUPIED")
        void markOutOfServiceFromOccupiedThrows() {
            ParkingSlot slot = createOccupiedSlot();

            assertThrows(InvalidSlotTransitionException.class, slot::markOutOfService);
        }
    }

    @Nested
    @DisplayName("currentVehicle")
    class CurrentVehicleTests {

        @Test
        @DisplayName("returns empty when slot is available")
        void returnsEmptyWhenAvailable() {
            ParkingSlot slot = createSlot(SlotStatus.AVAILABLE, SlotType.REGULAR);

            assertTrue(slot.getCurrentVehicle().isEmpty());
        }

        @Test
        @DisplayName("returns present when slot is occupied")
        void returnsPresentWhenOccupied() {
            ParkingSlot slot = createOccupiedSlot();

            assertFalse(slot.getCurrentVehicle().isEmpty());
            assertEquals("ABC-123", slot.getCurrentVehicle().orElseThrow().getLicensePlate().getValue());
        }
    }

    private ParkingSlot createSlot(SlotStatus status, SlotType slotType) {
        ParkingSlot slot = new ParkingSlot("slot-1", "A1", slotType, SlotStatus.AVAILABLE, "level-1");
        if (status == SlotStatus.RESERVED) {
            slot.reserve();
        } else if (status == SlotStatus.OUT_OF_SERVICE) {
            slot.markOutOfService();
        } else if (status == SlotStatus.OCCUPIED) {
            slot.occupy(createVehicle(VehicleType.CAR, "ABC-123"));
        }
        return slot;
    }

    private ParkingSlot createOccupiedSlot() {
        return createSlot(SlotStatus.OCCUPIED, SlotType.REGULAR);
    }

    private Vehicle createVehicle(VehicleType vehicleType, String plate) {
        return new Vehicle(new LicensePlate(plate), vehicleType, 30);
    }
}
