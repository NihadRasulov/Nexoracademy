package az.demo.NexoraAcademy.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request is well-formed but violates a business rule tied to
 * the current state of the data (e.g. enrolling into a full course group,
 * refunding more than was paid, moving an enrollment through an illegal
 * status transition).
 */
public class InvalidStateException extends ApiException {

    public InvalidStateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
