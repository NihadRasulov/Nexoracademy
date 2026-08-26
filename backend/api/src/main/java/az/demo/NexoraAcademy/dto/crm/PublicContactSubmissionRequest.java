package az.demo.NexoraAcademy.dto.crm;

import az.demo.NexoraAcademy.validation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Public form payload does not allow callers to attach or alter CRM lead IDs. */
public record PublicContactSubmissionRequest(
        @NotBlank @Size(min = 2, max = 150) String fullName,
        @Email @Size(max = 255) String email,
        @PhoneNumber String phone,
        @Size(max = 4000) String message,
        UUID courseId) {
}
