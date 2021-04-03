package services.parkingService;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import services.parkingService.exceptions.DuplicateRegistrationIDException;
import services.parkingService.exceptions.ParkingLotFullException;
import services.parkingService.exceptions.SpotAlreadyVacantException;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class ParkingLotServiceTest {
    private final int SAMPLE_COUNT = 100;
    private final int PARKING_LOT_CAPACITY = 100;
    private final int MIN_AGE = 18;
    private final int MAX_AGE = 60;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String randomVehicleId() {
        return UUID.randomUUID().toString();
    }

    private int randomIntegerInRange(int min, int max) {
        return (int)(Math.random()*(max-min+1) + min);
    }

    private void fillParkingSlot(IParkingLotService parkingLotService) {
        for (int i=0; i<PARKING_LOT_CAPACITY; ++i) {
            try {
                parkingLotService.park(randomVehicleId(), randomIntegerInRange(MIN_AGE, MAX_AGE));
            } catch (ParkingLotFullException e) {
                e.printStackTrace();
            } catch (DuplicateRegistrationIDException e) {
                --i;
            }
        }
    }

    @Test
    public void vehicleIsAlwaysParkedAtNearestAvailableSlot() {
        ParkingLotService parkingLotService = ParkingLotService.getInstance(PARKING_LOT_CAPACITY);

        fillParkingSlot(parkingLotService);

        for (int testIteration = SAMPLE_COUNT; testIteration > 0; --testIteration) {
            int numSlots = randomIntegerInRange(1, PARKING_LOT_CAPACITY);
            Set<Integer> vacantSlots = new TreeSet<>();

            for (int i=0; i<numSlots; ++i) {
                int slotId = randomIntegerInRange(1, PARKING_LOT_CAPACITY);
                try {
                    parkingLotService.unpark(slotId);
                    vacantSlots.add(slotId);
                } catch (SpotAlreadyVacantException e) {
                    --i;
                }
            }

            Iterator<Integer> iterator = vacantSlots.iterator();
            while (iterator.hasNext()) {
                boolean parked = false;
                while(!parked) {
                    String vehicleId = randomVehicleId();
                    try {
                        parkingLotService.park(vehicleId, randomIntegerInRange(MIN_AGE, MAX_AGE));
                        int slotForVehicleWithRegID = parkingLotService.getSlotForVehicleWithRegID(vehicleId);
                        Assert.assertTrue(iterator.next() == slotForVehicleWithRegID);
                        parked = true;
                    } catch (ParkingLotFullException e) {
                        e.printStackTrace();
                    } catch (DuplicateRegistrationIDException e) {
                    }
                }
            }
        }
    }

    @Test
    public void duplicateVehicleRegistrationIdExceptionIsThrown() throws
            DuplicateRegistrationIDException,
            ParkingLotFullException {

        ParkingLotService parkingLotService = ParkingLotService.getInstance(PARKING_LOT_CAPACITY);
        String vehicleId = randomVehicleId();

        parkingLotService.park(vehicleId,randomIntegerInRange(MIN_AGE, MAX_AGE));
        int slotForVehicleWithRegID = parkingLotService.getSlotForVehicleWithRegID(vehicleId);
        expectedException.expect(DuplicateRegistrationIDException.class);
        expectedException.expectMessage(String.format("The vehicle with ID '%s' " +
                "is already parked at Slot with ID '%d'!"+
                " Duplicate Registration Id! Call Police!", vehicleId, slotForVehicleWithRegID));
        parkingLotService.park(vehicleId, randomIntegerInRange(MIN_AGE, MAX_AGE));
    }

    @Test
    public void parkingLotFullExceptionThrown() throws ParkingLotFullException {
        ParkingLotService parkingLotService = ParkingLotService.getInstance(PARKING_LOT_CAPACITY);

        fillParkingSlot(parkingLotService);

        final int numSlots = PARKING_LOT_CAPACITY/2;
        for (int i=0; i<numSlots; ++i) {
            int slotId = randomIntegerInRange(1, PARKING_LOT_CAPACITY);
            try {
                parkingLotService.unpark(slotId);
            } catch (SpotAlreadyVacantException e) {
                --i;
            }
        }
        expectedException.expect(ParkingLotFullException.class);
        expectedException.expectMessage("Parking Lot is full!");

        for (int i=0; i<=numSlots; ++i) { // Note: we are filling (numSlots+1) number of slots
            try {
                parkingLotService.park(randomVehicleId(), randomIntegerInRange(MIN_AGE, MAX_AGE));
            }
            catch (DuplicateRegistrationIDException e) {
                --i;
                e.printStackTrace();
            }
        }
    }

    @Test
    public void slotAlreadyVacantExceptionIsThrown() throws SpotAlreadyVacantException {
        ParkingLotService parkingLotService = ParkingLotService.getInstance(PARKING_LOT_CAPACITY);
        fillParkingSlot(parkingLotService);
        int vacantSlot = randomIntegerInRange(1, PARKING_LOT_CAPACITY);

        expectedException.expect(SpotAlreadyVacantException.class);
        expectedException.expectMessage(String.format("Slot with id '%s' is vacant!", vacantSlot));

        parkingLotService.unpark(vacantSlot);
        parkingLotService.unpark(vacantSlot);
    }

    @Test
    public void getSlotsForDriverWithAgeTest() {
        ParkingLotService parkingLotService = ParkingLotService.getInstance(PARKING_LOT_CAPACITY);
        fillParkingSlot(parkingLotService);
        int total = 0;
        for (int i=MIN_AGE; i<= MAX_AGE; ++i) {
            total += parkingLotService.getSlotsForDriverWithAge(i).size();
        }
        Assert.assertTrue(total == PARKING_LOT_CAPACITY);
    }

    @Test
    public void getVehicleRegIDsForDriverWithAge() {
        ParkingLotService parkingLotService = ParkingLotService.getInstance(PARKING_LOT_CAPACITY);
        fillParkingSlot(parkingLotService);
        int total = 0;
        for (int i=MIN_AGE; i<= MAX_AGE; ++i) {
            total += parkingLotService.getVehicleRegIDsForDriverWithAge(i).size();
        }
        Assert.assertTrue(total == PARKING_LOT_CAPACITY);
    }

    @Test
    public void getSlotForVehicleWithRegID() throws DuplicateRegistrationIDException, ParkingLotFullException {
        ParkingLotService parkingLotService = ParkingLotService.getInstance(PARKING_LOT_CAPACITY);

        String vehicleId = randomVehicleId();
        parkingLotService.park(vehicleId, randomIntegerInRange(MIN_AGE, MAX_AGE));

        int slotForVehicleWithRegID = parkingLotService.getSlotForVehicleWithRegID(vehicleId);

        Assert.assertTrue(slotForVehicleWithRegID == 1);

        slotForVehicleWithRegID = parkingLotService.getSlotForVehicleWithRegID("");
        Assert.assertTrue(slotForVehicleWithRegID == -1);

    }
}
