package com.parkinglot.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ParkingTicket")
class ParkingTicketTest {

    @Nested
    @DisplayName("creation")
    class CreationTests {

        @Test
        @DisplayName("creates ticket with valid data")
        void creationWithValidData() {
            UUID ticketId = UUID.randomUUID();
            Instant entryTime = Instant.parse("2024-05-01T10:15:30Z");
            Vehicle vehicle = createVehicle("CAR-777");

            ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, "slot-1", "level-1", entryTime, TicketStatus.ACTIVE);

            assertEquals(ticketId, ticket.getTicketId());
            assertEquals(vehicle, ticket.getVehicle());
            assertEquals("slot-1", ticket.getSlotId());
            assertEquals("level-1", ticket.getLevelId());
            assertEquals(entryTime, ticket.getEntryTime());
            assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
            assertTrue(ticket.getExitTime().isEmpty());
        }
    }

    @Nested
    @DisplayName("close")
    class CloseTests {

        @Test
        @DisplayName("sets exit time and status to COMPLETED")
        void closeSetsExitTimeAndStatusToCompleted() {
            ParkingTicket ticket = createActiveTicket();
            Instant exitTime = ticket.getEntryTime().plus(Duration.ofHours(2));

            ticket.close(exitTime);

            assertEquals(TicketStatus.COMPLETED, ticket.getStatus());
            assertEquals(exitTime, ticket.getExitTime().orElseThrow());
        }

        @Test
        @DisplayName("throws when already completed")
        void closeOnAlreadyCompletedTicketThrowsException() {
            ParkingTicket ticket = createActiveTicket();
            ticket.close(ticket.getEntryTime().plus(Duration.ofMinutes(30)));

            assertThrows(IllegalStateException.class,
                    () -> ticket.close(ticket.getEntryTime().plus(Duration.ofHours(1))));
        }
    }

    @Test
    @DisplayName("returns correct duration")
    void getDurationReturnsCorrectDuration() {
        Instant entryTime = Instant.parse("2024-05-01T08:00:00Z");
        ParkingTicket ticket = new ParkingTicket(
                UUID.randomUUID(),
                createVehicle("CAR-888"),
                "slot-9",
                "level-2",
                entryTime,
                TicketStatus.ACTIVE);

        ticket.close(entryTime.plus(Duration.ofMinutes(90)));

        assertEquals(Duration.ofMinutes(90), ticket.getDuration());
    }

    private ParkingTicket createActiveTicket() {
        return new ParkingTicket(
                UUID.randomUUID(),
                createVehicle("CAR-123"),
                "slot-1",
                "level-1",
                Instant.parse("2024-05-01T10:00:00Z"),
                TicketStatus.ACTIVE);
    }

    private Vehicle createVehicle(String plate) {
        return new Vehicle(new LicensePlate(plate), VehicleType.CAR, 35);
    }
}
