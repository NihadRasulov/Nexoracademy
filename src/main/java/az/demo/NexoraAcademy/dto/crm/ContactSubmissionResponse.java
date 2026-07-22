package az.demo.NexoraAcademy.dto.crm;

import az.demo.NexoraAcademy.entity.enums.SubmissionType;

import java.time.Instant;
import java.util.UUID;

public record ContactSubmissionResponse(
        UUID id,
        UUID leadId,
        SubmissionType type,
        UUID courseId,
        String fullName,
        String email,
        String phone,
        String message,
        Instant preferredTime,
        String status,
        Instant submittedAt
) {
}
