package com.parkinglot.domain.model;

/**
 * Enumerates the supported parking slot types.
 */
public enum SlotType {
    COMPACT,
    REGULAR,
    LARGE,
    HANDICAPPED,
    EV_CHARGING,
    MOTORCYCLE;

    /**
     * Determines whether this slot type can accommodate the supplied vehicle type.
     *
     * @param vehicleType the incoming vehicle type
     * @return {@code true} when the slot supports the vehicle type
     */
    public boolean supports(final VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return this == MOTORCYCLE || this == COMPACT;
            case CAR:
                return this == COMPACT || this == REGULAR || this == LARGE || this == HANDICAPPED;
            case TRUCK:
            case VAN:
                return this == LARGE;
            case ELECTRIC:
                return this == EV_CHARGING || this == COMPACT || this == REGULAR || this == LARGE;
            default:
                return false;
        }
    }
}
