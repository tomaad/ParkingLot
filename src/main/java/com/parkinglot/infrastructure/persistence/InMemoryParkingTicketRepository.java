package com.parkinglot.infrastructure.persistence;

import com.parkinglot.domain.model.LicensePlate;
import com.parkinglot.domain.model.ParkingTicket;
import com.parkinglot.domain.repository.ParkingTicketRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for parking tickets.
 */
public final class InMemoryParkingTicketRepository implements ParkingTicketRepository {
    private final ConcurrentMap<String, ParkingTicket> storage = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ParkingTicket> findById(final String ticketId) {
        return Optional.ofNullable(storage.get(Objects.requireNonNull(ticketId, "ticketId must not be null")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParkingTicket save(final ParkingTicket parkingTicket) {
        final ParkingTicket value = Objects.requireNonNull(parkingTicket, "parkingTicket must not be null");
        storage.put(value.getTicketId().toString(), value);
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ParkingTicket> findActiveByVehicle(final LicensePlate licensePlate) {
        final LicensePlate plate = Objects.requireNonNull(licensePlate, "licensePlate must not be null");
        return storage.values().stream()
                .filter(ParkingTicket::isActive)
                .filter(ticket -> ticket.getVehicle().getLicensePlate().equals(plate))
                .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ParkingTicket> findAllActive() {
        return storage.values().stream()
                .filter(ParkingTicket::isActive)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
