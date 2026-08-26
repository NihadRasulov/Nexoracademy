package az.demo.NexoraAcademy.dto.auth;

import az.demo.NexoraAcademy.validation.PersonName;
import az.demo.NexoraAcademy.validation.PhoneNumber;
import az.demo.NexoraAcademy.validation.TrimmedStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
        // 254 = RFC 5321 maximum length of an email address.
        @NotBlank @Email @Size(max = 254)
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String email,

        @NotBlank @PersonName
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String firstName,

        @NotBlank @PersonName
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String lastName,

        @PhoneNumber
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String phone,

        // Not trimmed: leading/trailing spaces are legitimate password characters.
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "password must be 8-72 characters and contain at least one letter and one digit"
        )
        String password
) {
}
