package az.demo.NexoraAcademy.dto.identity;

import az.demo.NexoraAcademy.entity.enums.SessionType;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID userId,
        SessionType type,
        String ipAddress,
        String userAgent,
        Instant issuedAt,
        Instant expiresAt,
        Instant usedAt,
        Instant revokedAt
) {
}
