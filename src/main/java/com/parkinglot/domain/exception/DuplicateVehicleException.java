package com.parkinglot.domain.exception;

/**
 * Thrown when the same vehicle is already parked.
 */
public final class DuplicateVehicleException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message exception message
     */
    public DuplicateVehicleException(final String message) {
        super(message);
    }
}
