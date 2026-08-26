package az.demo.NexoraAcademy.dto.academics;

import az.demo.NexoraAcademy.entity.enums.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID userId,
        UUID groupId,
        EnrollmentStatus status,
        String idempotencyKey,
        String consentVersion,
        Instant consentGivenAt,
        Instant holdExpiresAt,
        Instant enrolledAt,
        Instant completedAt,
        Instant cancelledAt,
        String cancelReason
) {
}
