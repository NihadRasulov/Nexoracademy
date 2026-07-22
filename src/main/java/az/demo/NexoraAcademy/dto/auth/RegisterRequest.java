package az.demo.NexoraAcademy.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Public self-registration payload. Deliberately has no role/status fields —
 * self-registered accounts are always created as STUDENT/PENDING_VERIFICATION;
 * anything more privileged goes through the admin-only UserController.
 */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,

        @NotBlank @Size(min = 2, max = 150) String fullName,

        @Pattern(regexp = "^\\+?[0-9 ()-]{6,20}$", message = "phone must be a valid phone number")
        String phone,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "password must be 8-72 characters and contain at least one letter and one digit"
        )
        String password
) {
}
