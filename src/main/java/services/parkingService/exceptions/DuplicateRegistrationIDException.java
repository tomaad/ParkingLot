package services.parkingService.exceptions;

public class DuplicateRegistrationIDException extends Exception {
    public DuplicateRegistrationIDException() {
    }

    public DuplicateRegistrationIDException(String message) {
        super(message);
    }
}
