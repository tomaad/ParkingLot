package com.parkinglot.application.service;

import com.parkinglot.application.dto.ParkRequest;
import com.parkinglot.application.dto.ParkResponse;
import com.parkinglot.application.event.DomainEventPublisher;
import com.parkinglot.domain.event.DomainEvent;
import com.parkinglot.domain.event.LotCapacityEvent;
import com.parkinglot.domain.event.VehicleParkedEvent;
import com.parkinglot.domain.exception.DuplicateVehicleException;
import com.parkinglot.domain.exception.ParkingLotFullException;
import com.parkinglot.domain.model.LicensePlate;
import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.SlotStatus;
import com.parkinglot.domain.model.SlotType;
import com.parkinglot.domain.model.TicketStatus;
import com.parkinglot.domain.model.VehicleType;
import com.parkinglot.domain.policy.NearestSlotStrategy;
import com.parkinglot.domain.repository.ParkingLotRepository;
import com.parkinglot.domain.repository.ParkingTicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParkVehicleUseCase")
class ParkVehicleUseCaseTest {

    private static final String LOT_ID = "lot-1";

    @Mock
    private ParkingLotRepository parkingLotRepository;
    @Mock
    private ParkingTicketRepository parkingTicketRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

    @Nested
    @DisplayName("execute")
    class ExecuteTests {

        @Test
        @DisplayName("returns ticket info when parking succeeds")
        void successfulParkReturnsTicketInfo() {
            ParkingLot parkingLot = createTwoSlotParkingLot();
            when(parkingLotRepository.findById(LOT_ID)).thenReturn(Optional.of(parkingLot));
            when(parkingTicketRepository.findActiveByVehicle(new LicensePlate("ABC-123"))).thenReturn(Optional.empty());
            when(parkingTicketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(parkingLotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            ParkVehicleUseCase useCase = new ParkVehicleUseCase(
                    parkingLotRepository,
                    parkingTicketRepository,
                    new NearestSlotStrategy(),
                    eventPublisher,
                    LOT_ID,
                    0.9d);

            ParkResponse response = useCase.execute(new ParkRequest("ABC-123", VehicleType.CAR, 30));

            assertNotNull(response.getTicketId());
            assertEquals("A1", response.getSlotNumber());
            assertEquals(1, response.getLevelNumber());
            assertNotNull(response.getEntryTime());
        }

        @Test
        @DisplayName("throws DuplicateVehicleException when vehicle is already parked")
        void throwsDuplicateVehicleExceptionWhenVehicleAlreadyParked() {
            ParkingLot parkingLot = createParkingLotWithAvailableSlot();
            when(parkingLotRepository.findById(LOT_ID)).thenReturn(Optional.of(parkingLot));
            when(parkingTicketRepository.findActiveByVehicle(any())).thenReturn(Optional.of(TestFixtures.activeTicket("ABC-123", "slot-9", "level-2")));

            ParkVehicleUseCase useCase = new ParkVehicleUseCase(
                    parkingLotRepository,
                    parkingTicketRepository,
                    new NearestSlotStrategy(),
                    eventPublisher,
                    LOT_ID,
                    0.9d);

            assertThrows(DuplicateVehicleException.class,
                    () -> useCase.execute(new ParkRequest("ABC-123", VehicleType.CAR, 30)));
        }

        @Test
        @DisplayName("throws ParkingLotFullException when no slot is available")
        void throwsParkingLotFullExceptionWhenNoSlotAvailable() {
            ParkingLot parkingLot = createFullParkingLot();
            when(parkingLotRepository.findById(LOT_ID)).thenReturn(Optional.of(parkingLot));
            when(parkingTicketRepository.findActiveByVehicle(any())).thenReturn(Optional.empty());

            ParkVehicleUseCase useCase = new ParkVehicleUseCase(
                    parkingLotRepository,
                    parkingTicketRepository,
                    new NearestSlotStrategy(),
                    eventPublisher,
                    LOT_ID,
                    0.9d);

            assertThrows(ParkingLotFullException.class,
                    () -> useCase.execute(new ParkRequest("ZZZ-999", VehicleType.CAR, 27)));
        }

        @Test
        @DisplayName("publishes VehicleParkedEvent on success")
        void publishesVehicleParkedEventOnSuccess() {
            ParkingLot parkingLot = createTwoSlotParkingLot();
            when(parkingLotRepository.findById(LOT_ID)).thenReturn(Optional.of(parkingLot));
            when(parkingTicketRepository.findActiveByVehicle(any())).thenReturn(Optional.empty());
            when(parkingTicketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(parkingLotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            ParkVehicleUseCase useCase = new ParkVehicleUseCase(
                    parkingLotRepository,
                    parkingTicketRepository,
                    new NearestSlotStrategy(),
                    eventPublisher,
                    LOT_ID,
                    1.0d);

            useCase.execute(new ParkRequest("EV-101", VehicleType.CAR, 29));

            ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());
            DomainEvent event = eventCaptor.getValue();
            assertInstanceOf(VehicleParkedEvent.class, event);
            VehicleParkedEvent parkedEvent = (VehicleParkedEvent) event;
            assertEquals("EV-101", parkedEvent.getVehicleId());
            assertEquals("slot-1", parkedEvent.getSlotId());
            assertEquals("level-1", parkedEvent.getLevelId());
        }

        @Test
        @DisplayName("publishes LotCapacityEvent when threshold is exceeded")
        void publishesLotCapacityEventWhenThresholdExceeded() {
            ParkingLot parkingLot = createTwoSlotParkingLot();
            when(parkingLotRepository.findById(LOT_ID)).thenReturn(Optional.of(parkingLot));
            when(parkingTicketRepository.findActiveByVehicle(any())).thenReturn(Optional.empty());
            when(parkingTicketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(parkingLotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            ParkVehicleUseCase useCase = new ParkVehicleUseCase(
                    parkingLotRepository,
                    parkingTicketRepository,
                    new NearestSlotStrategy(),
                    eventPublisher,
                    LOT_ID,
                    0.5d);

            useCase.execute(new ParkRequest("CAP-500", VehicleType.CAR, 41));

            ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher, times(2)).publish(eventCaptor.capture());
            assertInstanceOf(VehicleParkedEvent.class, eventCaptor.getAllValues().get(0));
            assertInstanceOf(LotCapacityEvent.class, eventCaptor.getAllValues().get(1));
            LotCapacityEvent capacityEvent = (LotCapacityEvent) eventCaptor.getAllValues().get(1);
            assertEquals(LOT_ID, capacityEvent.getLotId());
            assertEquals(1, capacityEvent.getCurrentOccupancy());
            assertEquals(2, capacityEvent.getTotalCapacity());
        }
    }

    private ParkingLot createParkingLotWithAvailableSlot() {
        return new ParkingLot(
                LOT_ID,
                "Downtown",
                "123 Main St",
                List.of(new ParkingLevel("level-1", 1, List.of(
                        new ParkingSlot("slot-1", "A1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1")))));
    }

    private ParkingLot createTwoSlotParkingLot() {
        return new ParkingLot(
                LOT_ID,
                "Downtown",
                "123 Main St",
                List.of(new ParkingLevel("level-1", 1, List.of(
                        new ParkingSlot("slot-1", "A1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1"),
                        new ParkingSlot("slot-2", "A2", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1")))));
    }

    private ParkingLot createFullParkingLot() {
        ParkingSlot slot = new ParkingSlot("slot-1", "A1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1");
        slot.occupy(TestFixtures.vehicle("FIL-111", VehicleType.CAR));
        return new ParkingLot(
                LOT_ID,
                "Downtown",
                "123 Main St",
                List.of(new ParkingLevel("level-1", 1, List.of(slot))));
    }

    private static final class TestFixtures {
        private static com.parkinglot.domain.model.Vehicle vehicle(String plate, VehicleType vehicleType) {
            return new com.parkinglot.domain.model.Vehicle(new LicensePlate(plate), vehicleType, 30);
        }

        private static com.parkinglot.domain.model.ParkingTicket activeTicket(String plate, String slotId, String levelId) {
            return new com.parkinglot.domain.model.ParkingTicket(
                    UUID.randomUUID(),
                    vehicle(plate, VehicleType.CAR),
                    slotId,
                    levelId,
                    java.time.Instant.parse("2024-05-01T10:00:00Z"),
                    TicketStatus.ACTIVE);
        }
    }
}
