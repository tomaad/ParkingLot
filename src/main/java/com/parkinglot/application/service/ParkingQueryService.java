package com.parkinglot.application.service;

import com.parkinglot.application.dto.AvailabilityResponse;
import com.parkinglot.domain.model.LicensePlate;
import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.model.ParkingTicket;
import com.parkinglot.domain.repository.ParkingLotRepository;
import com.parkinglot.domain.repository.ParkingTicketRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Read-only application service for querying parking data.
 */
public final class ParkingQueryService {
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingTicketRepository parkingTicketRepository;

    /**
     * Creates the query service.
     *
     * @param parkingLotRepository lot repository
     * @param parkingTicketRepository ticket repository
     */
    public ParkingQueryService(final ParkingLotRepository parkingLotRepository,
                               final ParkingTicketRepository parkingTicketRepository) {
        this.parkingLotRepository = Objects.requireNonNull(parkingLotRepository, "parkingLotRepository must not be null");
        this.parkingTicketRepository = Objects.requireNonNull(parkingTicketRepository, "parkingTicketRepository must not be null");
    }

    /**
     * Returns availability metrics for the specified lot.
     *
     * @param lotId lot identifier
     * @return availability response
     */
    public AvailabilityResponse getAvailability(final String lotId) {
        final ParkingLot parkingLot = parkingLotRepository.findById(Objects.requireNonNull(lotId, "lotId must not be null"))
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found: " + lotId));
        final int totalSlots = parkingLot.getTotalCapacity();
        final int availableSlots = parkingLot.getAvailableSlotCount();
        final double occupancyRate = totalSlots == 0 ? 0.0d : (totalSlots - availableSlots) / (double) totalSlots;
        final Map<Integer, Integer> levelAvailability = parkingLot.getLevels().stream()
                .sorted(java.util.Comparator.comparingInt(ParkingLevel::getFloorNumber))
                .collect(Collectors.toMap(
                        ParkingLevel::getFloorNumber,
                        level -> level.getAvailableSlots().size(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        return new AvailabilityResponse(totalSlots, availableSlots, occupancyRate, levelAvailability);
    }

    /**
     * Finds the active ticket for a vehicle when present.
     *
     * @param licensePlate raw license plate value
     * @return active ticket when present
     */
    public Optional<ParkingTicket> findVehicle(final String licensePlate) {
        return parkingTicketRepository.findActiveByVehicle(new LicensePlate(licensePlate));
    }

    /**
     * Returns all active tickets.
     *
     * @return active tickets
     */
    public List<ParkingTicket> getActiveTickets() {
        return parkingTicketRepository.findAllActive();
    }
}
