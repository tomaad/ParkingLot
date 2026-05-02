package com.parkinglot.domain.repository;

import com.parkinglot.domain.model.LicensePlate;
import com.parkinglot.domain.model.ParkingTicket;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for parking ticket entities.
 */
public interface ParkingTicketRepository {
    /**
     * Finds a ticket by identifier.
     *
     * @param ticketId ticket identifier
     * @return matching ticket when present
     */
    Optional<ParkingTicket> findById(String ticketId);

    /**
     * Saves a parking ticket.
     *
     * @param parkingTicket ticket to save
     * @return saved ticket
     */
    ParkingTicket save(ParkingTicket parkingTicket);

    /**
     * Finds an active ticket by vehicle license plate.
     *
     * @param licensePlate license plate
     * @return matching active ticket when present
     */
    Optional<ParkingTicket> findActiveByVehicle(LicensePlate licensePlate);

    /**
     * Returns all active tickets.
     *
     * @return active tickets
     */
    List<ParkingTicket> findAllActive();
}
