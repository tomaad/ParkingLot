package com.parkinglot.application.service;

import com.parkinglot.application.dto.UnparkRequest;
import com.parkinglot.application.dto.UnparkResponse;
import com.parkinglot.application.event.DomainEventPublisher;
import com.parkinglot.domain.event.VehicleUnparkedEvent;
import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.ParkingTicket;
import com.parkinglot.domain.repository.ParkingLotRepository;
import com.parkinglot.domain.repository.ParkingTicketRepository;

import java.time.Instant;
import java.util.Objects;

/**
 * Application service that orchestrates unparking a vehicle.
 */
public final class UnparkVehicleUseCase {
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    private final DomainEventPublisher eventPublisher;
    private final String lotId;

    /**
     * Creates the use case.
     *
     * @param parkingLotRepository lot repository
     * @param parkingTicketRepository ticket repository
     * @param eventPublisher event publisher
     * @param lotId target lot identifier
     */
    public UnparkVehicleUseCase(final ParkingLotRepository parkingLotRepository,
                                final ParkingTicketRepository parkingTicketRepository,
                                final DomainEventPublisher eventPublisher,
                                final String lotId) {
        this.parkingLotRepository = Objects.requireNonNull(parkingLotRepository, "parkingLotRepository must not be null");
        this.parkingTicketRepository = Objects.requireNonNull(parkingTicketRepository, "parkingTicketRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.lotId = Objects.requireNonNull(lotId, "lotId must not be null");
    }

    /**
     * Unparks a vehicle for the supplied ticket.
     *
     * @param request unpark request
     * @return unpark response
     */
    public UnparkResponse execute(final UnparkRequest request) {
        final UnparkRequest unparkRequest = Objects.requireNonNull(request, "request must not be null");
        final ParkingTicket ticket = parkingTicketRepository.findById(unparkRequest.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + unparkRequest.getTicketId()));
        if (!ticket.isActive()) {
            throw new IllegalStateException("Ticket is not active: " + ticket.getTicketId());
        }

        final ParkingLot parkingLot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + lotId));
        final ParkingSlot slot = parkingLot.findSlotById(ticket.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + ticket.getSlotId()));

        slot.vacate();
        final Instant exitTime = Instant.now();
        ticket.close(exitTime);

        parkingTicketRepository.save(ticket);
        parkingLotRepository.save(parkingLot);
        eventPublisher.publish(new VehicleUnparkedEvent(
                ticket.getTicketId().toString(),
                ticket.getVehicle().getLicensePlate().getValue(),
                ticket.getSlotId(),
                ticket.getDuration(),
                exitTime));

        return new UnparkResponse(ticket.getTicketId().toString(), ticket.getDuration(), exitTime);
    }
}
