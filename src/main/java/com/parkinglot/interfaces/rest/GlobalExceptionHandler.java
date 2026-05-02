package com.parkinglot.interfaces.rest;

import com.parkinglot.domain.exception.DuplicateVehicleException;
import com.parkinglot.domain.exception.ParkingLotFullException;
import com.parkinglot.domain.exception.SlotNotOccupiedException;
import com.parkinglot.infrastructure.observability.ParkingMetrics;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final ParkingMetrics parkingMetrics;

    public GlobalExceptionHandler(final ParkingMetrics parkingMetrics) {
        this.parkingMetrics = Objects.requireNonNull(parkingMetrics, "parkingMetrics must not be null");
    }

    @ExceptionHandler(ParkingLotFullException.class)
    public ResponseEntity<ErrorResponse> handleParkingLotFull(final ParkingLotFullException exception,
                                                              final HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, exception, request, "parking_lot_full", false);
    }

    @ExceptionHandler(DuplicateVehicleException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateVehicle(final DuplicateVehicleException exception,
                                                                final HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, exception, request, "duplicate_vehicle", false);
    }

    @ExceptionHandler(SlotNotOccupiedException.class)
    public ResponseEntity<ErrorResponse> handleSlotNotOccupied(final SlotNotOccupiedException exception,
                                                               final HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request, "slot_not_occupied", false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(final IllegalArgumentException exception,
                                                               final HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request, "illegal_argument", false);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(final IllegalStateException exception,
                                                            final HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, exception, request, "illegal_state", false);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(final Exception exception,
                                                                final HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, request, "unexpected_error", true);
    }

    private ResponseEntity<ErrorResponse> buildResponse(final HttpStatus status,
                                                        final Exception exception,
                                                        final HttpServletRequest request,
                                                        final String errorType,
                                                        final boolean logAsError) {
        final String correlationId = resolveCorrelationId(request);
        final String path = request == null ? "unknown" : request.getRequestURI();
        final String message = exception.getMessage() == null || exception.getMessage().isBlank()
                ? status.getReasonPhrase()
                : exception.getMessage();
        parkingMetrics.recordError(errorType);
        MDC.put("correlationId", correlationId);
        try {
            if (logAsError) {
                LOGGER.error("Request failed correlationId={} path={} status={} message={}",
                        correlationId,
                        path,
                        status.value(),
                        message,
                        exception);
            } else {
                LOGGER.warn("Request failed correlationId={} path={} status={} message={}",
                        correlationId,
                        path,
                        status.value(),
                        message);
            }

            final ErrorResponse body = new ErrorResponse(
                    Instant.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    path);
            final HttpHeaders headers = new HttpHeaders();
            headers.add(CORRELATION_HEADER, correlationId);
            return new ResponseEntity<>(body, headers, status);
        } finally {
            MDC.remove("correlationId");
        }
    }

    private String resolveCorrelationId(final HttpServletRequest request) {
        if (request == null) {
            return UUID.randomUUID().toString();
        }
        final Object attribute = request.getAttribute(CORRELATION_HEADER);
        if (attribute instanceof String value && !value.isBlank()) {
            return value;
        }
        final String header = request.getHeader(CORRELATION_HEADER);
        if (header != null && !header.isBlank()) {
            return header;
        }
        return UUID.randomUUID().toString();
    }
}
