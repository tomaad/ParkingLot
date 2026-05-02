package com.parkinglot.application.service;

import com.parkinglot.application.dto.UnparkRequest;
import com.parkinglot.application.dto.UnparkResponse;
import com.parkinglot.application.event.DomainEventPublisher;
import com.parkinglot.domain.event.VehicleUnparkedEvent;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnparkVehicleUseCase")
class UnparkVehicleUseCaseTest {

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
        @DisplayName("returns duration and exit time when unparking succeeds")
        void successfulUnparkReturnsDurationAndExitTime() {
            ParkingSlot slot = occupiedSlot();
            ParkingTicket ticket = activeTicket(slot);
            ParkingLot parkingLot = lotWithSlot(slot);
            when(parkingTicketRepository.findById(ticket.getTicketId().toString())).thenReturn(Optional.of(ticket));
            when(parkingLotRepository.findById(LOT_ID)).thenReturn(Optional.of(parkingLot));
            when(parkingTicketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(parkingLotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            UnparkVehicleUseCase useCase = new UnparkVehicleUseCase(parkingLotRepository, parkingTicketRepository, eventPublisher, LOT_ID);

            UnparkResponse response = useCase.execute(new UnparkRequest(ticket.getTicketId().toString()));

            assertEquals(ticket.getTicketId().toString(), response.getTicketId());
            assertEquals(ticket.getExitTime().orElseThrow(), response.getExitTime());
            assertEquals(ticket.getDuration(), response.getDuration());
        }

        @Test
        @DisplayName("throws when ticket is not found")
        void throwsExceptionWhenTicketNotFound() {
            when(parkingTicketRepository.findById("missing-ticket")).thenReturn(Optional.empty());
            UnparkVehicleUseCase useCase = new UnparkVehicleUseCase(parkingLotRepository, parkingTicketRepository, eventPublisher, LOT_ID);

            assertThrows(IllegalArgumentException.class, () -> useCase.execute(new UnparkRequest("missing-ticket")));
        }

        @Test
        @DisplayName("throws when ticket is already completed")
        void throwsExceptionWhenTicketAlreadyCompleted() {
            ParkingSlot slot = occupiedSlot();
            ParkingTicket ticket = activeTicket(slot);
            ticket.close(ticket.getEntryTime().plusSeconds(300));
            when(parkingTicketRepository.findById(ticket.getTicketId().toString())).thenReturn(Optional.of(ticket));
            UnparkVehicleUseCase useCase = new UnparkVehicleUseCase(parkingLotRepository, parkingTicketRepository, eventPublisher, LOT_ID);

            assertThrows(IllegalStateException.class,
                    () -> useCase.execute(new UnparkRequest(ticket.getTicketId().toString())));
        }

        @Test
        @DisplayName("publishes VehicleUnparkedEvent on success")
        void publishesVehicleUnparkedEventOnSuccess() {
            ParkingSlot slot = occupiedSlot();
            ParkingTicket ticket = activeTicket(slot);
            ParkingLot parkingLot = lotWithSlot(slot);
            when(parkingTicketRepository.findById(ticket.getTicketId().toString())).thenReturn(Optional.of(ticket));
            when(parkingLotRepository.findById(LOT_ID)).thenReturn(Optional.of(parkingLot));
            when(parkingTicketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(parkingLotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            UnparkVehicleUseCase useCase = new UnparkVehicleUseCase(parkingLotRepository, parkingTicketRepository, eventPublisher, LOT_ID);

            useCase.execute(new UnparkRequest(ticket.getTicketId().toString()));

            ArgumentCaptor<com.parkinglot.domain.event.DomainEvent> eventCaptor = ArgumentCaptor.forClass(com.parkinglot.domain.event.DomainEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());
            assertInstanceOf(VehicleUnparkedEvent.class, eventCaptor.getValue());
            VehicleUnparkedEvent event = (VehicleUnparkedEvent) eventCaptor.getValue();
            assertEquals(ticket.getTicketId().toString(), event.getTicketId());
            assertEquals(ticket.getVehicle().getLicensePlate().getValue(), event.getVehicleId());
            assertEquals(ticket.getSlotId(), event.getSlotId());
        }

        @Test
        @DisplayName("vacates the slot")
        void vacatesTheSlot() {
            ParkingSlot slot = occupiedSlot();
            ParkingTicket ticket = activeTicket(slot);
            ParkingLot parkingLot = lotWithSlot(slot);
            when(parkingTicketRepository.findById(ticket.getTicketId().toString())).thenReturn(Optional.of(ticket));
            when(parkingLotRepository.findById(LOT_ID)).thenReturn(Optional.of(parkingLot));
            when(parkingTicketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(parkingLotRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            UnparkVehicleUseCase useCase = new UnparkVehicleUseCase(parkingLotRepository, parkingTicketRepository, eventPublisher, LOT_ID);

            useCase.execute(new UnparkRequest(ticket.getTicketId().toString()));

            assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
            assertTrue(slot.getCurrentVehicle().isEmpty());
            assertFalse(ticket.isActive());
        }
    }

    private ParkingSlot occupiedSlot() {
        ParkingSlot slot = new ParkingSlot("slot-1", "A1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1");
        slot.occupy(new Vehicle(new LicensePlate("ABC-123"), VehicleType.CAR, 32));
        return slot;
    }

    private ParkingTicket activeTicket(ParkingSlot slot) {
        return new ParkingTicket(
                UUID.randomUUID(),
                slot.getCurrentVehicle().orElseThrow(),
                slot.getSlotId(),
                slot.getLevelId(),
                Instant.parse("2024-05-01T10:00:00Z"),
                TicketStatus.ACTIVE);
    }

    private ParkingLot lotWithSlot(ParkingSlot slot) {
        return new ParkingLot(LOT_ID, "Downtown", "123 Main St", List.of(new ParkingLevel("level-1", 1, List.of(slot))));
    }
}
