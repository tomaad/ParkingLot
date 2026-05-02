package com.parkinglot.infrastructure.persistence;

import com.parkinglot.domain.model.LicensePlate;
import com.parkinglot.domain.model.ParkingTicket;
import com.parkinglot.domain.model.TicketStatus;
import com.parkinglot.domain.model.Vehicle;
import com.parkinglot.domain.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InMemoryParkingTicketRepository")
class InMemoryParkingTicketRepositoryTest {

    private InMemoryParkingTicketRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingTicketRepository();
    }

    @Test
    @DisplayName("saves and finds by id")
    void saveAndFindById() {
        ParkingTicket ticket = createTicket("ABC-123", TicketStatus.ACTIVE);

        repository.save(ticket);

        assertTrue(repository.findById(ticket.getTicketId().toString()).isPresent());
        assertSame(ticket, repository.findById(ticket.getTicketId().toString()).orElseThrow());
    }

    @Test
    @DisplayName("findActiveByVehicle finds active ticket")
    void findActiveByVehicleFindsActiveTicket() {
        ParkingTicket ticket = createTicket("ABC-123", TicketStatus.ACTIVE);
        repository.save(ticket);

        assertSame(ticket, repository.findActiveByVehicle(new LicensePlate("ABC-123")).orElseThrow());
    }

    @Test
    @DisplayName("findActiveByVehicle returns empty for completed tickets")
    void findActiveByVehicleReturnsEmptyForCompletedTickets() {
        ParkingTicket ticket = createTicket("ABC-123", TicketStatus.ACTIVE);
        ticket.close(ticket.getEntryTime().plusSeconds(60));
        repository.save(ticket);

        assertTrue(repository.findActiveByVehicle(new LicensePlate("ABC-123")).isEmpty());
    }

    @Test
    @DisplayName("findAllActive returns only active tickets")
    void findAllActiveReturnsOnlyActiveTickets() {
        ParkingTicket active = createTicket("AAA-111", TicketStatus.ACTIVE);
        ParkingTicket completed = createTicket("BBB-222", TicketStatus.ACTIVE);
        completed.close(completed.getEntryTime().plusSeconds(60));
        repository.save(active);
        repository.save(completed);

        assertEquals(1, repository.findAllActive().size());
        assertSame(active, repository.findAllActive().get(0));
    }

    private ParkingTicket createTicket(String plate, TicketStatus status) {
        ParkingTicket ticket = new ParkingTicket(
                UUID.randomUUID(),
                new Vehicle(new LicensePlate(plate), VehicleType.CAR, 31),
                "slot-1",
                "level-1",
                Instant.parse("2024-05-01T10:00:00Z"),
                TicketStatus.ACTIVE);
        if (status == TicketStatus.COMPLETED) {
            ticket.close(ticket.getEntryTime().plusSeconds(60));
        }
        return ticket;
    }
}
