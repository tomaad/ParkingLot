package services.parkingService;

import services.parkingService.exceptions.DuplicateRegistrationIDException;
import services.parkingService.exceptions.ParkingLotFullException;
import services.parkingService.exceptions.SpotAlreadyVacantException;

import java.util.List;

public interface IParkingLotService {

    void park(String vehicleRegId, int driverAge) throws ParkingLotFullException, DuplicateRegistrationIDException;
    void unpark(int slotId) throws SpotAlreadyVacantException;
    List<String> getSlotsForDriverWithAge(int age);
    List<String> getVehicleRegIDsForDriverWithAge(int age);
    int getSlotForVehicleWithRegID(String regId);
}
