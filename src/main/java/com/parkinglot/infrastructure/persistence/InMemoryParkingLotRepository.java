package com.parkinglot.infrastructure.persistence;

import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.repository.ParkingLotRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory repository for parking lot aggregates.
 */
public final class InMemoryParkingLotRepository implements ParkingLotRepository {
    private final ConcurrentMap<String, ParkingLot> storage = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ParkingLot> findById(final String lotId) {
        return Optional.ofNullable(storage.get(Objects.requireNonNull(lotId, "lotId must not be null")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParkingLot save(final ParkingLot parkingLot) {
        final ParkingLot value = Objects.requireNonNull(parkingLot, "parkingLot must not be null");
        storage.put(value.getLotId(), value);
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ParkingLot> findAll() {
        return new ArrayList<>(storage.values());
    }
}
