package com.parkinglot.application.dto;

import com.parkinglot.domain.model.VehicleType;

import java.util.Objects;

/**
 * Request DTO for parking a vehicle.
 */
public final class ParkRequest {
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final int driverAge;

    /**
     * Creates a park request.
     *
     * @param licensePlate vehicle license plate
     * @param vehicleType vehicle type
     * @param driverAge driver age
     */
    public ParkRequest(final String licensePlate, final VehicleType vehicleType, final int driverAge) {
        this.licensePlate = Objects.requireNonNull(licensePlate, "licensePlate must not be null");
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType must not be null");
        this.driverAge = driverAge;
    }

    /**
     * Returns the license plate.
     *
     * @return license plate
     */
    public String getLicensePlate() {
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
     * @return driver age
     */
    public int getDriverAge() {
        return driverAge;
    }
}
