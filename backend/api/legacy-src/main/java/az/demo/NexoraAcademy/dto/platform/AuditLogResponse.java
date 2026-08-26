package az.demo.NexoraAcademy.dto.platform;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        Long id,
        UUID actorId,
        String action,
        String entityType,
        String entityId,
        Map<String, Object> beforeState,
        Map<String, Object> afterState,
        UUID traceId,
        String ipAddress,
        Instant createdAt
) {
}
