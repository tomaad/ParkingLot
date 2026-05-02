package com.parkinglot.config;

import com.parkinglot.application.event.DomainEventPublisher;
import com.parkinglot.application.service.ParkVehicleUseCase;
import com.parkinglot.application.service.ParkingQueryService;
import com.parkinglot.application.service.UnparkVehicleUseCase;
import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.SlotStatus;
import com.parkinglot.domain.model.SlotType;
import com.parkinglot.domain.policy.LevelBalancedStrategy;
import com.parkinglot.domain.policy.NearestSlotStrategy;
import com.parkinglot.domain.policy.SlotAllocationStrategy;
import com.parkinglot.domain.repository.ParkingLotRepository;
import com.parkinglot.domain.repository.ParkingTicketRepository;
import com.parkinglot.infrastructure.config.ParkingLotConfig;
import com.parkinglot.infrastructure.event.SimpleEventPublisher;
import com.parkinglot.infrastructure.observability.ParkingMetrics;
import com.parkinglot.infrastructure.persistence.InMemoryParkingLotRepository;
import com.parkinglot.infrastructure.persistence.InMemoryParkingTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
public class ParkingLotSpringConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingLotSpringConfig.class);
    private static final String DEFAULT_LOT_ID = "PARKING-LOT-1";
    private static final String DEFAULT_LOT_ADDRESS = "100 Main Street";

    @Bean(name = "parkingLotId")
    public String parkingLotId() {
        return DEFAULT_LOT_ID;
    }

    @Bean
    public ParkingLotConfig parkingLotConfig(
            @Value("${parking-lot.name}") final String lotName,
            @Value("${parking-lot.levels}") final int levels,
            @Value("${parking-lot.slots-per-level}") final int slotsPerLevel,
            @Value("${parking-lot.allocation-strategy}") final String allocationStrategy,
            @Value("${parking-lot.capacity-alert-threshold}") final double capacityAlertThreshold) {
        return new ParkingLotConfig(
                lotName,
                levels,
                slotsPerLevel,
                buildSlotDistribution(slotsPerLevel),
                allocationStrategy,
                capacityAlertThreshold);
    }

    @Bean
    public ParkingLot parkingLot(final ParkingLotConfig config,
                                 @Qualifier("parkingLotId") final String lotId) {
        return new ParkingLot(lotId, config.getLotName(), DEFAULT_LOT_ADDRESS, buildLevels(config));
    }

    @Bean
    public ParkingLotRepository parkingLotRepository(final ParkingLot parkingLot,
                                                     final ParkingMetrics parkingMetrics) {
        final InMemoryParkingLotRepository repository = new InMemoryParkingLotRepository();
        repository.save(parkingLot);
        parkingMetrics.updateOccupancy(parkingLot.getTotalCapacity() - parkingLot.getAvailableSlotCount(),
                parkingLot.getTotalCapacity());
        LOGGER.info("Initialized parking lot name={} lotId={} levels={} totalCapacity={}",
                parkingLot.getName(),
                parkingLot.getLotId(),
                parkingLot.getLevels().size(),
                parkingLot.getTotalCapacity());
        return repository;
    }

    @Bean
    public ParkingTicketRepository parkingTicketRepository() {
        return new InMemoryParkingTicketRepository();
    }

    @Bean
    public DomainEventPublisher domainEventPublisher() {
        return new SimpleEventPublisher();
    }

    @Bean
    public SlotAllocationStrategy slotAllocationStrategy(final ParkingLotConfig config) {
        return resolveStrategy(config.getAllocationStrategyName());
    }

    @Bean
    public ParkVehicleUseCase parkVehicleUseCase(final ParkingLotRepository parkingLotRepository,
                                                 final ParkingTicketRepository parkingTicketRepository,
                                                 final SlotAllocationStrategy slotAllocationStrategy,
                                                 final DomainEventPublisher domainEventPublisher,
                                                 final ParkingLotConfig config,
                                                 @Qualifier("parkingLotId") final String lotId) {
        return new ParkVehicleUseCase(
                parkingLotRepository,
                parkingTicketRepository,
                slotAllocationStrategy,
                domainEventPublisher,
                lotId,
                config.getCapacityAlertThreshold());
    }

    @Bean
    public UnparkVehicleUseCase unparkVehicleUseCase(final ParkingLotRepository parkingLotRepository,
                                                     final ParkingTicketRepository parkingTicketRepository,
                                                     final DomainEventPublisher domainEventPublisher,
                                                     @Qualifier("parkingLotId") final String lotId) {
        return new UnparkVehicleUseCase(parkingLotRepository, parkingTicketRepository, domainEventPublisher, lotId);
    }

    @Bean
    public ParkingQueryService parkingQueryService(final ParkingLotRepository parkingLotRepository,
                                                   final ParkingTicketRepository parkingTicketRepository) {
        return new ParkingQueryService(parkingLotRepository, parkingTicketRepository);
    }

    private SlotAllocationStrategy resolveStrategy(final String strategyName) {
        final String normalized = strategyName == null ? "nearest" : strategyName.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "nearest" -> new NearestSlotStrategy();
            case "balanced" -> new LevelBalancedStrategy();
            default -> throw new IllegalArgumentException("Unsupported allocation strategy: " + strategyName);
        };
    }

    private Map<SlotType, Integer> buildSlotDistribution(final int slotsPerLevel) {
        final Map<SlotType, Integer> distribution = new LinkedHashMap<>();
        final SlotType[] orderedTypes = {
                SlotType.COMPACT,
                SlotType.REGULAR,
                SlotType.LARGE,
                SlotType.HANDICAPPED,
                SlotType.EV_CHARGING,
                SlotType.MOTORCYCLE
        };
        final double[] weights = {0.20d, 0.40d, 0.20d, 0.05d, 0.10d, 0.05d};

        int allocated = 0;
        for (int index = 0; index < orderedTypes.length; index++) {
            final int count;
            if (index == orderedTypes.length - 1) {
                count = slotsPerLevel - allocated;
            } else {
                count = (int) Math.floor(slotsPerLevel * weights[index]);
                allocated += count;
            }
            distribution.put(orderedTypes[index], Math.max(0, count));
        }
        return distribution;
    }

    private List<ParkingLevel> buildLevels(final ParkingLotConfig config) {
        final List<ParkingLevel> levels = new ArrayList<>();
        for (int floor = 1; floor <= config.getLevelsCount(); floor++) {
            final String levelId = "LEVEL-" + floor;
            final List<ParkingSlot> slots = new ArrayList<>();
            int slotSequence = 1;
            for (Map.Entry<SlotType, Integer> entry : config.getSlotTypesDistribution().entrySet()) {
                for (int index = 0; index < entry.getValue(); index++) {
                    final String slotId = levelId + "-SLOT-" + slotSequence;
                    final String slotNumber = String.format("F%d-S%02d", floor, slotSequence);
                    slots.add(new ParkingSlot(slotId, slotNumber, entry.getKey(), SlotStatus.AVAILABLE, levelId));
                    slotSequence++;
                }
            }
            levels.add(new ParkingLevel(levelId, floor, slots));
        }
        return levels;
    }
}
