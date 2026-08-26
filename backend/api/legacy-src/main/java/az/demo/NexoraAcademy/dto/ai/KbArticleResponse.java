package az.demo.NexoraAcademy.dto.ai;

import java.time.Instant;
import java.util.UUID;

public record KbArticleResponse(
        UUID id,
        String sourceType,
        String sourceRefId,
        String title,
        String content,
        Boolean active,
        Instant updatedAt
) {
}
