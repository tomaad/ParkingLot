package services.parkingService.exceptions;

public class ParkingLotFullException extends Exception {
    
    private static final long serialVersionUID = 5978868444626427019L;

    public ParkingLotFullException() {
        super("Parking Lot is Full!");
    }

    public ParkingLotFullException(String message) {
        super(message);
    }
}
