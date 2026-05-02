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
import com.parkinglot.infrastructure.observability.ParkingMetrics;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/parking", produces = MediaType.APPLICATION_JSON_VALUE)
public final class ParkingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingController.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final ParkVehicleUseCase parkVehicleUseCase;
    private final UnparkVehicleUseCase unparkVehicleUseCase;
    private final ParkingQueryService parkingQueryService;
    private final ParkingMetrics parkingMetrics;
    private final String lotId;

    public ParkingController(final ParkVehicleUseCase parkVehicleUseCase,
                             final UnparkVehicleUseCase unparkVehicleUseCase,
                             final ParkingQueryService parkingQueryService,
                             final ParkingMetrics parkingMetrics,
                             @Qualifier("parkingLotId") final String lotId) {
        this.parkVehicleUseCase = Objects.requireNonNull(parkVehicleUseCase, "parkVehicleUseCase must not be null");
        this.unparkVehicleUseCase = Objects.requireNonNull(unparkVehicleUseCase, "unparkVehicleUseCase must not be null");
        this.parkingQueryService = Objects.requireNonNull(parkingQueryService, "parkingQueryService must not be null");
        this.parkingMetrics = Objects.requireNonNull(parkingMetrics, "parkingMetrics must not be null");
        this.lotId = Objects.requireNonNull(lotId, "lotId must not be null");
    }

    @PostMapping(path = "/park", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ParkResponse> park(@RequestBody final ParkRequest request,
                                             @RequestHeader(value = CORRELATION_HEADER, required = false) final String correlationId,
                                             final HttpServletRequest httpServletRequest) {
        final String resolvedCorrelationId = initializeCorrelation(httpServletRequest, correlationId);
        final long startNanos = System.nanoTime();
        try {
            LOGGER.info("Park request received correlationId={} path={} licensePlate={} vehicleType={}",
                    resolvedCorrelationId,
                    httpServletRequest.getRequestURI(),
                    request.getLicensePlate(),
                    request.getVehicleType());
            final ParkResponse response = parkVehicleUseCase.execute(request);
            parkingMetrics.recordPark(Duration.ofNanos(System.nanoTime() - startNanos));
            refreshOccupancyMetrics();
            LOGGER.info("Park request completed correlationId={} ticketId={} slot={} level={}",
                    resolvedCorrelationId,
                    response.getTicketId(),
                    response.getSlotNumber(),
                    response.getLevelNumber());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(CORRELATION_HEADER, resolvedCorrelationId)
                    .body(response);
        } finally {
            clearCorrelation();
        }
    }

    @PostMapping(path = "/unpark", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UnparkResponse> unpark(@RequestBody final UnparkRequest request,
                                                 @RequestHeader(value = CORRELATION_HEADER, required = false) final String correlationId,
                                                 final HttpServletRequest httpServletRequest) {
        final String resolvedCorrelationId = initializeCorrelation(httpServletRequest, correlationId);
        final long startNanos = System.nanoTime();
        try {
            LOGGER.info("Unpark request received correlationId={} path={} ticketId={}",
                    resolvedCorrelationId,
                    httpServletRequest.getRequestURI(),
                    request.getTicketId());
            final UnparkResponse response = unparkVehicleUseCase.execute(request);
            parkingMetrics.recordUnpark(Duration.ofNanos(System.nanoTime() - startNanos));
            refreshOccupancyMetrics();
            LOGGER.info("Unpark request completed correlationId={} ticketId={} durationSeconds={}",
                    resolvedCorrelationId,
                    response.getTicketId(),
                    response.getDuration().toSeconds());
            return ResponseEntity.ok()
                    .header(CORRELATION_HEADER, resolvedCorrelationId)
                    .body(response);
        } finally {
            clearCorrelation();
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> availability(
            @RequestHeader(value = CORRELATION_HEADER, required = false) final String correlationId,
            final HttpServletRequest httpServletRequest) {
        final String resolvedCorrelationId = initializeCorrelation(httpServletRequest, correlationId);
        try {
            LOGGER.info("Availability request received correlationId={} path={}",
                    resolvedCorrelationId,
                    httpServletRequest.getRequestURI());
            final AvailabilityResponse response = parkingQueryService.getAvailability(lotId);
            updateOccupancyMetrics(response);
            LOGGER.info("Availability request completed correlationId={} availableSlots={} occupancyRate={}",
                    resolvedCorrelationId,
                    response.getAvailableSlots(),
                    response.getOccupancyRate());
            return ResponseEntity.ok()
                    .header(CORRELATION_HEADER, resolvedCorrelationId)
                    .body(response);
        } finally {
            clearCorrelation();
        }
    }

    @GetMapping("/vehicles/{licensePlate}")
    public ResponseEntity<VehicleLookupResponse> vehicle(@PathVariable final String licensePlate,
                                                         @RequestHeader(value = CORRELATION_HEADER, required = false) final String correlationId,
                                                         final HttpServletRequest httpServletRequest) {
        final String resolvedCorrelationId = initializeCorrelation(httpServletRequest, correlationId);
        try {
            LOGGER.info("Vehicle lookup request received correlationId={} path={} licensePlate={}",
                    resolvedCorrelationId,
                    httpServletRequest.getRequestURI(),
                    licensePlate);
            final Optional<ParkingTicket> ticket = parkingQueryService.findVehicle(licensePlate);
            if (ticket.isEmpty()) {
                LOGGER.info("Vehicle lookup request completed correlationId={} licensePlate={} found=false",
                        resolvedCorrelationId,
                        licensePlate);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header(CORRELATION_HEADER, resolvedCorrelationId)
                        .build();
            }

            final VehicleLookupResponse response = toVehicleLookupResponse(ticket.get());
            LOGGER.info("Vehicle lookup request completed correlationId={} licensePlate={} found=true ticketId={}",
                    resolvedCorrelationId,
                    licensePlate,
                    response.ticketId());
            return ResponseEntity.ok()
                    .header(CORRELATION_HEADER, resolvedCorrelationId)
                    .body(response);
        } finally {
            clearCorrelation();
        }
    }

    public record VehicleLookupResponse(String ticketId,
                                        String licensePlate,
                                        String vehicleType,
                                        int driverAge,
                                        String slotId,
                                        String levelId,
                                        Instant entryTime,
                                        Instant exitTime,
                                        String status) {
    }

    private VehicleLookupResponse toVehicleLookupResponse(final ParkingTicket ticket) {
        return new VehicleLookupResponse(
                ticket.getTicketId().toString(),
                ticket.getVehicle().getLicensePlate().getValue(),
                ticket.getVehicle().getVehicleType().name(),
                ticket.getVehicle().getDriverAge(),
                ticket.getSlotId(),
                ticket.getLevelId(),
                ticket.getEntryTime(),
                ticket.getExitTime().orElse(null),
                ticket.getStatus().name());
    }

    private void refreshOccupancyMetrics() {
        updateOccupancyMetrics(parkingQueryService.getAvailability(lotId));
    }

    private void updateOccupancyMetrics(final AvailabilityResponse response) {
        parkingMetrics.updateOccupancy(response.getTotalSlots() - response.getAvailableSlots(), response.getTotalSlots());
    }

    private String initializeCorrelation(final HttpServletRequest request, final String correlationId) {
        final String resolvedCorrelationId = correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId.trim();
        request.setAttribute(CORRELATION_HEADER, resolvedCorrelationId);
        MDC.put("correlationId", resolvedCorrelationId);
        return resolvedCorrelationId;
    }

    private void clearCorrelation() {
        MDC.remove("correlationId");
    }
}
