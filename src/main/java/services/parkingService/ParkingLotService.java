package services.parkingService;

import constants.Constants;
import factory.AppLogger;
import factory.Factory;
import services.parkingService.exceptions.DuplicateRegistrationIDException;
import services.parkingService.exceptions.ParkingLotFullException;
import services.parkingService.exceptions.SpotAlreadyVacantException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

public class ParkingLotService implements IParkingLotService {
    private static final AppLogger log = Factory.getAppLogger(Factory.getSlf4jLogger(ParkingLotService.class));

    private int maxSlots;
    private int size;
    Map<String, Integer> vehicleIdToDriverAgeMap;
    Map<String, Integer> vehicleIdToSlotMap;
    Map<Integer, String> slotToVehicleIdMap;
    Queue<Integer> vacantSlotsQueue;

    private ParkingLotService(int maxSlots) {
        this.maxSlots = maxSlots;
        this.vehicleIdToDriverAgeMap = new HashMap<>();
        this.vehicleIdToSlotMap = new HashMap<>();
        this.slotToVehicleIdMap = new HashMap<>();
        this.size = 0;

        this.vacantSlotsQueue = new PriorityQueue<>();
        for (int i=1; i <= maxSlots; ++i) {
            vacantSlotsQueue.add(i);
        }
    }

    public static ParkingLotService getInstance(int maxSlots) {
        log.debug(String.format("Created parking of %d slots", maxSlots));
        return new ParkingLotService(maxSlots);
    }

    @Override
    public void park(String vehicleRegId, int driverAge)
            throws ParkingLotFullException, DuplicateRegistrationIDException {
        // If the Parking Lot is full, then throw exception
        if (size == maxSlots) {
            throw new ParkingLotFullException("Parking Lot is full!");
        }

        // If the Vehicle with same registration ID is already parked, then throw exception
        if (vehicleIdToSlotMap.containsKey(vehicleRegId)) {
            throw new DuplicateRegistrationIDException(String.format("The vehicle with ID '%s' " +
                    "is already parked at Slot with ID '%d'!"+
                    "Duplicate Registration Id! Call Police!", vehicleRegId, vehicleIdToSlotMap.get(vehicleRegId)));
        }

        // If the pool of vacant slots is empty, then it's application logic failure
        if (vacantSlotsQueue.isEmpty()) {
            throw new RuntimeException("Parking Lot is not full yet there is no vacant slot! " +
                    "Application logic failure!");
        }

        int vacantSlot = vacantSlotsQueue.poll(); // nearest vacant slot

        if (!slotToVehicleIdMap.containsKey(vacantSlot)) {
            // assign nearest vacant slot to the vehicle
            slotToVehicleIdMap.put(vacantSlot, vehicleRegId);
            vehicleIdToSlotMap.put(vehicleRegId, vacantSlot);
        } else {
            // The logic has failed to determine nearest vacant slot
            throw new RuntimeException("The slot is already taken! Application logic failure!");
        }

        // Assign the driver age to the vehicle with the given ID,
        // duplicate should never exist or application logic failed
        if (!vehicleIdToDriverAgeMap.containsKey(vehicleRegId)) {
            vehicleIdToDriverAgeMap.put(vehicleRegId, driverAge);
        } else {
            throw new RuntimeException("The vehicle with ID is already present! Application Logic Failure!");
        }

        log.debug(String.format("Car with vehicle registration number \"%s\" " +
                "has been parked at slot number %s", vehicleRegId, vacantSlot));
        ++size;
    }

    @Override
    public void unpark(int slotId) throws SpotAlreadyVacantException {
        // If the parking lot is empty or the slot is already vacant, then throw exception
        if (size <=0 || !slotToVehicleIdMap.containsKey(slotId)) {
            throw new SpotAlreadyVacantException(String.format("Slot with id '%s' is vacant!", slotId));
        }

        // get the vehicle assigned to the slot
        String vehicleId = slotToVehicleIdMap.get(slotId);

        // If the driver age is not found for the vehicle, then it's application logic failure
        if (!vehicleIdToDriverAgeMap.containsKey(vehicleId)) {
            throw new RuntimeException(String.format("Driver not found for vehicle '%s'! " +
                    "Application logic failure!", vehicleId));
        }

        int driverAge = vehicleIdToDriverAgeMap.get(vehicleId);

        log.debug(String.format("Slot number %s vacated, " +
                "the car with vehicle registration number \"%s\" left the space, " +
                "the driver of the car was of age %s", slotId, vehicleId, driverAge));

        // Remove the vehicle from the record
        slotToVehicleIdMap.remove(slotId);
        vehicleIdToSlotMap.remove(vehicleId);
        vehicleIdToDriverAgeMap.remove(vehicleId);

        // Add the slot to the pool of available slots
        vacantSlotsQueue.add(slotId);

        --size;
    }

    @Override
    public List<String> getSlotsForDriverWithAge(int age) {
        List<String> vehicleRegIDsForDriverWithAge = getVehicleRegIDsForDriverWithAge(age);
        List<String> slots = vehicleRegIDsForDriverWithAge
                .stream()
                .map(vehicleId -> String.valueOf(vehicleIdToSlotMap.get(vehicleId))) // Get the slot for the vehicle
                .collect(Collectors.toList());
        log.debug(String.format("Slots for drivers with age = %s: [%s]", age, String.join(Constants.COMMA, slots)));
        return slots;
    }

    @Override
    public List<String> getVehicleRegIDsForDriverWithAge(int age) {
        List<String> vehicleList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : vehicleIdToDriverAgeMap.entrySet()) {
            if (entry.getValue() == age) {
                vehicleList.add(entry.getKey());
            }
        }
        vehicleList.sort((a, b) -> {
            if (vehicleIdToSlotMap.get(a) == vehicleIdToSlotMap.get(b))
                return 0;
            return vehicleIdToSlotMap.get(a) < vehicleIdToSlotMap.get(b) ? -1: 1;
        });
        log.debug(String.format("Vehicles with driver age = %s : [%s]", age,
                String.join(Constants.COMMA, vehicleList)));
        return vehicleList;
    }

    @Override
    public int getSlotForVehicleWithRegID(String regId) {
        int slot = -1;
        if (vehicleIdToSlotMap.containsKey(regId)) {
            slot = vehicleIdToSlotMap.get(regId);
        }
        if (slot !=- 1) {
            log.debug(String.valueOf(slot));
        } else {
            log.debug(String.format("Car with Registration ID \"%s\" does not exist in the Parking Lot!", regId));
        }

        return slot;
    }
}
