package com.parkinglot.infrastructure.observability;

import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.repository.ParkingLotRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ParkingLotHealthIndicator extends AbstractHealthIndicator {
    private final ParkingLotRepository parkingLotRepository;
    private final String lotId;

    public ParkingLotHealthIndicator(final ParkingLotRepository parkingLotRepository,
                                     @Qualifier("parkingLotId") final String lotId) {
        this.parkingLotRepository = Objects.requireNonNull(parkingLotRepository, "parkingLotRepository must not be null");
        this.lotId = Objects.requireNonNull(lotId, "lotId must not be null");
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        final ParkingLot parkingLot = parkingLotRepository.findById(lotId)
                .orElse(null);
        if (parkingLot == null) {
            builder.down()
                    .withDetail("reason", "Parking lot is not initialized")
                    .withDetail("lotId", lotId);
            return;
        }

        final int totalCapacity = parkingLot.getTotalCapacity();
        final int availableSlots = parkingLot.getAvailableSlotCount();
        final double occupancyRate = totalCapacity == 0 ? 0.0d : (totalCapacity - availableSlots) / (double) totalCapacity;
        final boolean overCapacityAlertThreshold = occupancyRate > 0.99d;
        final Health.Builder statusBuilder = overCapacityAlertThreshold ? builder.down() : builder.up();
        statusBuilder
                .withDetail("lotId", parkingLot.getLotId())
                .withDetail("totalCapacity", totalCapacity)
                .withDetail("availableSlots", availableSlots)
                .withDetail("occupancyRate", occupancyRate)
                .withDetail("levelsCount", parkingLot.getLevels().size());
        if (overCapacityAlertThreshold) {
            statusBuilder.withDetail("reason", "Occupancy exceeded 99% alert threshold");
        }
    }
}
