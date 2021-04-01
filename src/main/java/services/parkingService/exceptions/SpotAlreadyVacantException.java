package services.parkingService.exceptions;

public class SpotAlreadyVacantException extends Exception {
    public SpotAlreadyVacantException() {
    }

    public SpotAlreadyVacantException(String message) {
        super(message);
    }
}
