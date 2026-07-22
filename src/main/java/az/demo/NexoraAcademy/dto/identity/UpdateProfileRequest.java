package az.demo.NexoraAcademy.dto.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Self-service profile update. Deliberately excludes role/status/password —
 * those go through UserController (admin-only) or the dedicated
 * change-password endpoint, so a user can never escalate their own privileges
 * through /me.
 */
public record UpdateProfileRequest(
        @Email @Size(max = 255) String email,

        @Pattern(regexp = "^\\+?[0-9 ()-]{6,20}$", message = "phone must be a valid phone number")
        String phone,

        @Size(min = 2, max = 150) String fullName,

        @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "locale must look like 'az' or 'az-AZ'")
        String locale,

        @Size(max = 50) Map<String, Object> profile
) {
}
