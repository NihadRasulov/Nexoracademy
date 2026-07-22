package az.demo.NexoraAcademy.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public static DuplicateResourceException of(String entityName, String field, Object value) {
        return new DuplicateResourceException(entityName + " already exists with " + field + ": " + value);
    }
}
