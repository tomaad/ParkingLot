package com.parkinglot.application.service;

import com.parkinglot.application.dto.AvailabilityResponse;
import com.parkinglot.domain.model.LicensePlate;
import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.ParkingTicket;
import com.parkinglot.domain.model.SlotStatus;
import com.parkinglot.domain.model.SlotType;
import com.parkinglot.domain.model.TicketStatus;
import com.parkinglot.domain.model.Vehicle;
import com.parkinglot.domain.model.VehicleType;
import com.parkinglot.domain.repository.ParkingLotRepository;
import com.parkinglot.domain.repository.ParkingTicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkingQueryService")
class ParkingQueryServiceTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;
    @Mock
    private ParkingTicketRepository parkingTicketRepository;

    @Test
    @DisplayName("getAvailability returns correct counts")
    void getAvailabilityReturnsCorrectCounts() {
        ParkingLot parkingLot = createParkingLot();
        when(parkingLotRepository.findById("lot-1")).thenReturn(Optional.of(parkingLot));
        ParkingQueryService service = new ParkingQueryService(parkingLotRepository, parkingTicketRepository);

        AvailabilityResponse response = service.getAvailability("lot-1");

        assertEquals(3, response.getTotalSlots());
        assertEquals(2, response.getAvailableSlots());
        assertEquals(1.0d / 3.0d, response.getOccupancyRate());
        assertEquals(Map.of(1, 1, 2, 1), response.getLevelAvailability());
    }

    @Test
    @DisplayName("findVehicle finds active ticket")
    void findVehicleFindsActiveTicket() {
        ParkingTicket ticket = activeTicket("ABC-123");
        when(parkingTicketRepository.findActiveByVehicle(new LicensePlate("ABC-123"))).thenReturn(Optional.of(ticket));
        ParkingQueryService service = new ParkingQueryService(parkingLotRepository, parkingTicketRepository);

        Optional<ParkingTicket> result = service.findVehicle("ABC-123");

        assertTrue(result.isPresent());
        assertSame(ticket, result.orElseThrow());
    }

    @Test
    @DisplayName("findVehicle returns empty when not found")
    void findVehicleReturnsEmptyWhenNotFound() {
        when(parkingTicketRepository.findActiveByVehicle(new LicensePlate("MISSING-1"))).thenReturn(Optional.empty());
        ParkingQueryService service = new ParkingQueryService(parkingLotRepository, parkingTicketRepository);

        assertTrue(service.findVehicle("MISSING-1").isEmpty());
    }

    @Test
    @DisplayName("getActiveTickets returns all active tickets")
    void getActiveTicketsReturnsAllActive() {
        List<ParkingTicket> activeTickets = List.of(activeTicket("AAA-111"), activeTicket("BBB-222"));
        when(parkingTicketRepository.findAllActive()).thenReturn(activeTickets);
        ParkingQueryService service = new ParkingQueryService(parkingLotRepository, parkingTicketRepository);

        assertEquals(activeTickets, service.getActiveTickets());
    }

    private ParkingLot createParkingLot() {
        ParkingSlot occupied = new ParkingSlot("slot-1", "A1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1");
        occupied.occupy(new Vehicle(new LicensePlate("OCC-123"), VehicleType.CAR, 27));
        ParkingLevel levelOne = new ParkingLevel("level-1", 1, List.of(
                occupied,
                new ParkingSlot("slot-2", "A2", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1")));
        ParkingLevel levelTwo = new ParkingLevel("level-2", 2, List.of(
                new ParkingSlot("slot-3", "B1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-2")));
        return new ParkingLot("lot-1", "Downtown", "123 Main St", List.of(levelOne, levelTwo));
    }

    private ParkingTicket activeTicket(String plate) {
        return new ParkingTicket(
                UUID.randomUUID(),
                new Vehicle(new LicensePlate(plate), VehicleType.CAR, 29),
                "slot-1",
                "level-1",
                Instant.parse("2024-05-01T10:00:00Z"),
                TicketStatus.ACTIVE);
    }
}
