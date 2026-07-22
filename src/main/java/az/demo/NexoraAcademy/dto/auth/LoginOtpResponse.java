package az.demo.NexoraAcademy.dto.auth;

/** Returned by POST /auth/login once credentials check out — tokens are not issued yet,
 *  the client must submit the emailed code to POST /auth/login/verify-otp to get a TokenResponse. */
public record LoginOtpResponse(
        String message,
        String email,
        long expiresInSeconds
) {
}
