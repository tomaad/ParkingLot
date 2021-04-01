package services.parkingService.exceptions;

public class ParkingLotFullException extends Exception {
    public ParkingLotFullException() {
        super("Parking Lot is Full!");
    }

    public ParkingLotFullException(String message) {
        super(message);
    }
}
