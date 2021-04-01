package services.command;

import services.parkingService.IParkingLotService;
import services.parkingService.exceptions.DuplicateRegistrationIDException;
import services.parkingService.exceptions.ParkingLotFullException;
import services.parkingService.exceptions.SpotAlreadyVacantException;

public interface Command {
    void execute(IParkingLotService parkingLotService, String[] args)
            throws DuplicateRegistrationIDException,
            ParkingLotFullException,
            NumberFormatException,
            SpotAlreadyVacantException;
}
