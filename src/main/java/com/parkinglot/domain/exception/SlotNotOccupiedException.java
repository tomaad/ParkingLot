package com.parkinglot.domain.exception;

/**
 * Thrown when trying to vacate a slot that is not occupied.
 */
public final class SlotNotOccupiedException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param message exception message
     */
    public SlotNotOccupiedException(final String message) {
        super(message);
    }
}
