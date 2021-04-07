package services.parkingService.exceptions;

public class SpotAlreadyVacantException extends Exception {
    
    private static final long serialVersionUID = 3520273012004990001L;

    public SpotAlreadyVacantException() {
    }

    public SpotAlreadyVacantException(String message) {
        super(message);
    }
}
