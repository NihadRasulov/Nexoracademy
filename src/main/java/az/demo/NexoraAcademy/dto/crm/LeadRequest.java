package az.demo.NexoraAcademy.dto.crm;

import az.demo.NexoraAcademy.entity.enums.LeadSource;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record LeadRequest(
        @Size(min = 2, max = 150) String fullName,

        @Email @Size(max = 255) String email,

        @Pattern(regexp = "^\\+?[0-9 ()-]{6,20}$", message = "phone must be a valid phone number")
        String phone,

        UUID courseId,

        @NotNull(groups = ValidationGroups.OnCreate.class) LeadSource source,

        UUID assignedTo,

        @Size(max = 20) String consentVersion
) {
}
