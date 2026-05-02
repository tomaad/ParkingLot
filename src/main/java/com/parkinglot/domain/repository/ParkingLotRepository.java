package com.parkinglot.domain.repository;

import com.parkinglot.domain.model.ParkingLot;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for parking lot aggregates.
 */
public interface ParkingLotRepository {
    /**
     * Finds a lot by identifier.
     *
     * @param lotId lot identifier
     * @return matching lot when present
     */
    Optional<ParkingLot> findById(String lotId);

    /**
     * Saves a parking lot aggregate.
     *
     * @param parkingLot parking lot to save
     * @return saved parking lot
     */
    ParkingLot save(ParkingLot parkingLot);

    /**
     * Returns all parking lots.
     *
     * @return all parking lots
     */
    List<ParkingLot> findAll();
}
