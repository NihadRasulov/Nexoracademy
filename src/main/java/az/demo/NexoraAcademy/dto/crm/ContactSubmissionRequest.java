package az.demo.NexoraAcademy.dto.crm;

import az.demo.NexoraAcademy.entity.enums.SubmissionType;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record ContactSubmissionRequest(
        UUID leadId,

        @NotNull(groups = ValidationGroups.OnCreate.class) SubmissionType type,

        UUID courseId,

        @Size(min = 2, max = 150) String fullName,

        @Email @Size(max = 255) String email,

        @Pattern(regexp = "^\\+?[0-9 ()-]{6,20}$", message = "phone must be a valid phone number")
        String phone,

        @Size(max = 4000) String message,

        @FutureOrPresent Instant preferredTime
) {
}
