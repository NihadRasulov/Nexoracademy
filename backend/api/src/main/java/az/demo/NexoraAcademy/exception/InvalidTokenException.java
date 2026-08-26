package az.demo.NexoraAcademy.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown for a JWT/refresh-token that is malformed, expired, revoked, or
 * otherwise fails to authenticate a request.
 */
public class InvalidTokenException extends ApiException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
