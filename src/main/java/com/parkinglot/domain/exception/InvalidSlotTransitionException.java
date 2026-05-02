package com.parkinglot.domain.exception;

/**
 * Thrown when a parking slot receives an invalid state transition.
 */
public final class InvalidSlotTransitionException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message exception message
     */
    public InvalidSlotTransitionException(final String message) {
        super(message);
    }
}
