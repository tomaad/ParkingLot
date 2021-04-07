package services.parkingService.exceptions;

public class DuplicateRegistrationIDException extends Exception {
    
    private static final long serialVersionUID = 5704596574573607591L;

    public DuplicateRegistrationIDException() {
    }

    public DuplicateRegistrationIDException(String message) {
        super(message);
    }
}
