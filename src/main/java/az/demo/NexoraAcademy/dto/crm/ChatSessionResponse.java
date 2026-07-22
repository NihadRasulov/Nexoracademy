package az.demo.NexoraAcademy.dto.crm;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ChatSessionResponse(
        UUID id,
        UUID userId,
        UUID leadId,
        String channel,
        List<Map<String, Object>> messages,
        Instant startedAt,
        Instant endedAt
) {
}
