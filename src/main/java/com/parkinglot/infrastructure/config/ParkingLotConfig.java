package com.parkinglot.infrastructure.config;

import com.parkinglot.domain.model.SlotType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration holder for bootstrapping the parking lot.
 */
public final class ParkingLotConfig {
    private final String lotName;
    private final int levelsCount;
    private final int slotsPerLevel;
    private final Map<SlotType, Integer> slotTypesDistribution;
    private final String allocationStrategyName;
    private final double capacityAlertThreshold;

    /**
     * Creates the configuration.
     *
     * @param lotName lot name
     * @param levelsCount number of levels
     * @param slotsPerLevel slots per level
     * @param slotTypesDistribution per-level slot distribution
     * @param allocationStrategyName configured strategy name
     * @param capacityAlertThreshold threshold from 0 to 1
     */
    public ParkingLotConfig(final String lotName,
                            final int levelsCount,
                            final int slotsPerLevel,
                            final Map<SlotType, Integer> slotTypesDistribution,
                            final String allocationStrategyName,
                            final double capacityAlertThreshold) {
        this.lotName = requireText(lotName, "lotName");
        if (levelsCount <= 0) {
            throw new IllegalArgumentException("levelsCount must be positive");
        }
        if (slotsPerLevel <= 0) {
            throw new IllegalArgumentException("slotsPerLevel must be positive");
        }
        this.levelsCount = levelsCount;
        this.slotsPerLevel = slotsPerLevel;
        this.slotTypesDistribution = Collections.unmodifiableMap(new LinkedHashMap<>(
                Objects.requireNonNull(slotTypesDistribution, "slotTypesDistribution must not be null")));
        final int configuredSlots = this.slotTypesDistribution.values().stream().mapToInt(Integer::intValue).sum();
        if (configuredSlots != slotsPerLevel) {
            throw new IllegalArgumentException("slotTypesDistribution must sum to slotsPerLevel");
        }
        this.allocationStrategyName = requireText(allocationStrategyName, "allocationStrategyName");
        if (capacityAlertThreshold < 0.0d || capacityAlertThreshold > 1.0d) {
            throw new IllegalArgumentException("capacityAlertThreshold must be between 0 and 1");
        }
        this.capacityAlertThreshold = capacityAlertThreshold;
    }

    /**
     * Returns the lot name.
     *
     * @return lot name
     */
    public String getLotName() {
        return lotName;
    }

    /**
     * Returns the number of levels.
     *
     * @return levels count
     */
    public int getLevelsCount() {
        return levelsCount;
    }

    /**
     * Returns the slot count per level.
     *
     * @return slots per level
     */
    public int getSlotsPerLevel() {
        return slotsPerLevel;
    }

    /**
     * Returns the per-level slot distribution.
     *
     * @return slot distribution
     */
    public Map<SlotType, Integer> getSlotTypesDistribution() {
        return slotTypesDistribution;
    }

    /**
     * Returns the allocation strategy name.
     *
     * @return strategy name
     */
    public String getAllocationStrategyName() {
        return allocationStrategyName;
    }

    /**
     * Returns the capacity alert threshold.
     *
     * @return threshold from 0 to 1
     */
    public double getCapacityAlertThreshold() {
        return capacityAlertThreshold;
    }

    private static String requireText(final String value, final String fieldName) {
        final String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
