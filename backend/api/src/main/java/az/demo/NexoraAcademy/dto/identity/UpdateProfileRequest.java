package az.demo.NexoraAcademy.dto.identity;

import az.demo.NexoraAcademy.validation.PersonName;
import az.demo.NexoraAcademy.validation.PhoneNumber;
import az.demo.NexoraAcademy.validation.TrimmedStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Self-service profile update. Deliberately excludes role/status/password —
 * those go through UserController (admin-only) or the dedicated
 * change-password endpoint, so a user can never escalate their own privileges
 * through /me.
 *
 * Every field is optional (PATCH semantics: omitted = unchanged), so presence
 * constraints are absent by design; the format constraints below still run on
 * whichever fields the client actually sends.
 */
public record UpdateProfileRequest(
        @Email @Size(max = 254)
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String email,

        @PhoneNumber
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String phone,

        @PersonName
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String firstName,

        @PersonName
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String lastName,

        @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "locale must look like 'az' or 'az-AZ'")
        String locale,

        @Size(max = 50) Map<String, Object> profile
) {
}
