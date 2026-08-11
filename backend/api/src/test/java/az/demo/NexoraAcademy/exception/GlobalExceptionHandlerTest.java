package az.demo.NexoraAcademy.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void resourceNotFoundExceptionMapsTo404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApiException(new ResourceNotFoundException("Course not found with id: 123"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains("Course not found");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/test");
    }

    @Test
    void duplicateResourceExceptionMapsTo409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApiException(DuplicateResourceException.of("User", "email", "a@b.com"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void invalidCredentialsExceptionMapsTo401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApiException(new InvalidCredentialsException("bad creds"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void badCredentialsExceptionMapsTo401WithGenericMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(new BadCredentialsException("raw internal detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        // must not leak the internal Spring Security message
        assertThat(response.getBody().message()).isEqualTo("Invalid email or password");
    }

    @Test
    void dataIntegrityViolationMapsTo409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("constraint violated"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void unexpectedExceptionMapsTo500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("some internal secret"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).doesNotContain("internal secret");
    }
}
