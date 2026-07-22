package az.demo.NexoraAcademy.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Second step of login: the 6-digit code emailed after a successful email+password check. */
public record LoginOtpVerifyRequest(
        @NotBlank @Email String email,

        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "otp must be a 6-digit code")
        String otp
) {
}
