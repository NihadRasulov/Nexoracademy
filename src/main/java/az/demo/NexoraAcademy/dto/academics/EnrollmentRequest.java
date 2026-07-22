package az.demo.NexoraAcademy.dto.academics;

import az.demo.NexoraAcademy.entity.enums.EnrollmentStatus;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID userId,

        @NotNull(groups = ValidationGroups.OnCreate.class) UUID groupId,

        EnrollmentStatus status,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 8, max = 100)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "idempotencyKey must be alphanumeric (with - or _)")
        String idempotencyKey,

        @Size(max = 20) String consentVersion,

        @PastOrPresent Instant consentGivenAt
) {
}
