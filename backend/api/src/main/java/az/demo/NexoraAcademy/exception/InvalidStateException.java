package az.demo.NexoraAcademy.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request is well-formed but violates a business rule tied to
 * the current state of the data (for example, deleting a category that still
 * has courses).
 */
public class InvalidStateException extends ApiException {

    public InvalidStateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
