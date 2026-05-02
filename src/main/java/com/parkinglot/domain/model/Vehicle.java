package com.parkinglot.domain.model;

import java.util.Objects;

/**
 * Immutable vehicle value object.
 */
public final class Vehicle {
    private final LicensePlate licensePlate;
    private final VehicleType vehicleType;
    private final int driverAge;

    /**
     * Creates a vehicle descriptor.
     *
     * @param licensePlate vehicle registration identifier
     * @param vehicleType vehicle category
     * @param driverAge driver age in years
     */
    public Vehicle(final LicensePlate licensePlate, final VehicleType vehicleType, final int driverAge) {
        this.licensePlate = Objects.requireNonNull(licensePlate, "licensePlate must not be null");
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        if (driverAge <= 0) {
            throw new IllegalArgumentException("driverAge must be positive");
        }
        this.driverAge = driverAge;
    }

    /**
     * Returns the vehicle license plate.
     *
     * @return license plate value object
     */
    public LicensePlate getLicensePlate() {
        return licensePlate;
    }

    /**
     * Returns the vehicle type.
     *
     * @return vehicle type
     */
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    /**
     * Returns the driver age.
     *
     * @return driver age in years
     */
    public int getDriverAge() {
        return driverAge;
    }
}
