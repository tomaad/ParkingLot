package com.parkinglot.domain.exception;

/**
 * Thrown when no more slots are available in the parking lot.
 */
public final class ParkingLotFullException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message exception message
     */
    public ParkingLotFullException(final String message) {
        super(message);
    }
}
