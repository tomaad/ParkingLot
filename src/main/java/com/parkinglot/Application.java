package com.parkinglot;

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
import com.parkinglot.infrastructure.persistence.InMemoryParkingLotRepository;
import com.parkinglot.infrastructure.persistence.InMemoryParkingTicketRepository;
import com.parkinglot.interfaces.cli.CliAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application entry point that wires the modular monolith together.
 */
public final class Application {
    private Application() {
    }

    /**
     * Starts the parking lot application.
     *
     * @param args optional command file path
     * @throws IOException when the command source cannot be read
     */
    public static void main(final String[] args) throws IOException {
        final String lotId = UUID.randomUUID().toString();
        final ParkingLotConfig config = buildConfig();
        final ParkingLot parkingLot = new ParkingLot(lotId, config.getLotName(), "100 Main Street", buildLevels(config));

        final ParkingLotRepository parkingLotRepository = new InMemoryParkingLotRepository();
        final ParkingTicketRepository parkingTicketRepository = new InMemoryParkingTicketRepository();
        final SimpleEventPublisher eventPublisher = new SimpleEventPublisher();
        eventPublisher.registerListener(event -> System.out.println("EVENT => " + event.getEventType()));
        parkingLotRepository.save(parkingLot);

        final SlotAllocationStrategy strategy = resolveStrategy(config.getAllocationStrategyName());
        final ParkVehicleUseCase parkVehicleUseCase = new ParkVehicleUseCase(
                parkingLotRepository,
                parkingTicketRepository,
                strategy,
                eventPublisher,
                lotId,
                config.getCapacityAlertThreshold());
        final UnparkVehicleUseCase unparkVehicleUseCase = new UnparkVehicleUseCase(
                parkingLotRepository,
                parkingTicketRepository,
                eventPublisher,
                lotId);
        final ParkingQueryService parkingQueryService = new ParkingQueryService(parkingLotRepository, parkingTicketRepository);

        final Reader reader = args != null && args.length > 0
                ? Files.newBufferedReader(Paths.get(args[0]))
                : new InputStreamReader(System.in);
        new CliAdapter(parkVehicleUseCase, unparkVehicleUseCase, parkingQueryService, reader, System.out, lotId).start();
    }

    private static ParkingLotConfig buildConfig() {
        final Map<SlotType, Integer> distribution = new LinkedHashMap<SlotType, Integer>();
        distribution.put(SlotType.COMPACT, 4);
        distribution.put(SlotType.REGULAR, 8);
        distribution.put(SlotType.LARGE, 4);
        distribution.put(SlotType.HANDICAPPED, 1);
        distribution.put(SlotType.EV_CHARGING, 2);
        distribution.put(SlotType.MOTORCYCLE, 1);
        return new ParkingLotConfig("Downtown Parking Plaza", 3, 20, distribution, "nearest", 0.90d);
    }

    private static List<ParkingLevel> buildLevels(final ParkingLotConfig config) {
        final List<ParkingLevel> levels = new ArrayList<ParkingLevel>();
        for (int floor = 1; floor <= config.getLevelsCount(); floor++) {
            final String levelId = "LEVEL-" + floor;
            final List<ParkingSlot> slots = new ArrayList<ParkingSlot>();
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

    private static SlotAllocationStrategy resolveStrategy(final String strategyName) {
        if ("balanced".equalsIgnoreCase(strategyName)) {
            return new LevelBalancedStrategy();
        }
        return new NearestSlotStrategy();
    }
}
