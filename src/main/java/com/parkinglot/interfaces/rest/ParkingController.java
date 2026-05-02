package com.parkinglot.interfaces.rest;

import com.parkinglot.application.dto.AvailabilityResponse;
import com.parkinglot.application.dto.ParkRequest;
import com.parkinglot.application.dto.ParkResponse;
import com.parkinglot.application.dto.UnparkRequest;
import com.parkinglot.application.dto.UnparkResponse;
import com.parkinglot.application.service.ParkVehicleUseCase;
import com.parkinglot.application.service.ParkingQueryService;
import com.parkinglot.application.service.UnparkVehicleUseCase;
import com.parkinglot.domain.model.ParkingTicket;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Placeholder REST controller showing how the application layer can be exposed over HTTP.
 */
public final class ParkingController {
    private final ParkVehicleUseCase parkVehicleUseCase;
    private final UnparkVehicleUseCase unparkVehicleUseCase;
    private final ParkingQueryService parkingQueryService;
    private final String lotId;

    /**
     * Creates the controller.
     *
     * @param parkVehicleUseCase parking use case
     * @param unparkVehicleUseCase unparking use case
     * @param parkingQueryService query service
     * @param lotId lot identifier
     */
    public ParkingController(final ParkVehicleUseCase parkVehicleUseCase,
                             final UnparkVehicleUseCase unparkVehicleUseCase,
                             final ParkingQueryService parkingQueryService,
                             final String lotId) {
        this.parkVehicleUseCase = Objects.requireNonNull(parkVehicleUseCase, "parkVehicleUseCase must not be null");
        this.unparkVehicleUseCase = Objects.requireNonNull(unparkVehicleUseCase, "unparkVehicleUseCase must not be null");
        this.parkingQueryService = Objects.requireNonNull(parkingQueryService, "parkingQueryService must not be null");
        this.lotId = Objects.requireNonNull(lotId, "lotId must not be null");
    }

    /**
     * POST /park
     *
     * @param request park request body
     * @return park response body
     */
    public ParkResponse park(final ParkRequest request) {
        return parkVehicleUseCase.execute(request);
    }

    /**
     * POST /unpark
     *
     * @param request unpark request body
     * @return unpark response body
     */
    public UnparkResponse unpark(final UnparkRequest request) {
        return unparkVehicleUseCase.execute(request);
    }

    /**
     * GET /availability
     *
     * @return availability payload
     */
    public AvailabilityResponse availability() {
        return parkingQueryService.getAvailability(lotId);
    }

    /**
     * GET /vehicles/{plate}
     *
     * @param plate vehicle license plate
     * @return active ticket when present
     */
    public Optional<ParkingTicket> vehicle(final String plate) {
        return parkingQueryService.findVehicle(plate);
    }

    /**
     * GET /vehicles
     *
     * @return active tickets
     */
    public List<ParkingTicket> activeVehicles() {
        return parkingQueryService.getActiveTickets();
    }
}
