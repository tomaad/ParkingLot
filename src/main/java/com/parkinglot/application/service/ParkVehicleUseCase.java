package com.parkinglot.application.service;

import com.parkinglot.application.dto.ParkRequest;
import com.parkinglot.application.dto.ParkResponse;
import com.parkinglot.application.event.DomainEventPublisher;
import com.parkinglot.domain.event.LotCapacityEvent;
import com.parkinglot.domain.event.VehicleParkedEvent;
import com.parkinglot.domain.exception.DuplicateVehicleException;
import com.parkinglot.domain.exception.ParkingLotFullException;
import com.parkinglot.domain.model.LicensePlate;
import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.ParkingTicket;
import com.parkinglot.domain.model.TicketStatus;
import com.parkinglot.domain.model.Vehicle;
import com.parkinglot.domain.policy.SlotAllocationStrategy;
import com.parkinglot.domain.repository.ParkingLotRepository;
import com.parkinglot.domain.repository.ParkingTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service that orchestrates parking a vehicle.
 */
public final class ParkVehicleUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkVehicleUseCase.class);

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    private final SlotAllocationStrategy allocationStrategy;
    private final DomainEventPublisher eventPublisher;
    private final String lotId;
    private final double capacityAlertThreshold;

    /**
     * Creates the use case.
     *
     * @param parkingLotRepository lot repository
     * @param parkingTicketRepository ticket repository
     * @param allocationStrategy allocation strategy
     * @param eventPublisher domain event publisher
     * @param lotId target lot identifier
     * @param capacityAlertThreshold occupancy threshold from 0 to 1
     */
    public ParkVehicleUseCase(final ParkingLotRepository parkingLotRepository,
                              final ParkingTicketRepository parkingTicketRepository,
                              final SlotAllocationStrategy allocationStrategy,
                              final DomainEventPublisher eventPublisher,
                              final String lotId,
                              final double capacityAlertThreshold) {
        this.parkingLotRepository = Objects.requireNonNull(parkingLotRepository, "parkingLotRepository must not be null");
        this.parkingTicketRepository = Objects.requireNonNull(parkingTicketRepository, "parkingTicketRepository must not be null");
        this.allocationStrategy = Objects.requireNonNull(allocationStrategy, "allocationStrategy must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.lotId = Objects.requireNonNull(lotId, "lotId must not be null");
        if (capacityAlertThreshold < 0.0d || capacityAlertThreshold > 1.0d) {
            throw new IllegalArgumentException("capacityAlertThreshold must be between 0 and 1");
        }
        this.capacityAlertThreshold = capacityAlertThreshold;
    }

    /**
     * Parks a vehicle in the configured parking lot.
     *
     * @param request park request
     * @return parking response
     */
    public ParkResponse execute(final ParkRequest request) {
        final ParkRequest parkRequest = Objects.requireNonNull(request, "request must not be null");
        LOGGER.info("Attempting to park vehicle lotId={} licensePlate={} vehicleType={}",
                lotId,
                parkRequest.getLicensePlate(),
                parkRequest.getVehicleType());
        final ParkingLot parkingLot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + lotId));

        final LicensePlate licensePlate = new LicensePlate(parkRequest.getLicensePlate());
        parkingTicketRepository.findActiveByVehicle(licensePlate)
                .ifPresent(ticket -> {
                    throw new DuplicateVehicleException("Vehicle already parked with ticket " + ticket.getTicketId());
                });

        final Vehicle vehicle = new Vehicle(licensePlate, parkRequest.getVehicleType(), parkRequest.getDriverAge());
        final ParkingSlot slot = parkingLot.findAvailableSlot(vehicle.getVehicleType(), allocationStrategy)
                .orElseThrow(() -> new ParkingLotFullException("No compatible slot available for " + vehicle.getVehicleType()));
        slot.occupy(vehicle);

        final Instant entryTime = Instant.now();
        final ParkingTicket ticket = new ParkingTicket(
                UUID.randomUUID(),
                vehicle,
                slot.getSlotId(),
                slot.getLevelId(),
                entryTime,
                TicketStatus.ACTIVE);

        parkingTicketRepository.save(ticket);
        parkingLotRepository.save(parkingLot);
        eventPublisher.publish(new VehicleParkedEvent(
                ticket.getTicketId().toString(),
                vehicle.getLicensePlate().getValue(),
                slot.getSlotId(),
                slot.getLevelId(),
                entryTime));

        final int currentOccupancy = parkingLot.getTotalCapacity() - parkingLot.getAvailableSlotCount();
        if (parkingLot.getTotalCapacity() > 0
                && currentOccupancy / (double) parkingLot.getTotalCapacity() >= capacityAlertThreshold) {
            eventPublisher.publish(new LotCapacityEvent(
                    parkingLot.getLotId(),
                    currentOccupancy,
                    parkingLot.getTotalCapacity(),
                    entryTime));
        }

        final int levelNumber = parkingLot.findLevelById(slot.getLevelId())
                .map(ParkingLevel::getFloorNumber)
                .orElse(-1);
        LOGGER.info("Vehicle parked successfully lotId={} ticketId={} slotId={} levelId={} occupancy={}/{}",
                lotId,
                ticket.getTicketId(),
                slot.getSlotId(),
                slot.getLevelId(),
                currentOccupancy,
                parkingLot.getTotalCapacity());
        return new ParkResponse(ticket.getTicketId().toString(), slot.getSlotNumber(), levelNumber, entryTime);
    }
}
