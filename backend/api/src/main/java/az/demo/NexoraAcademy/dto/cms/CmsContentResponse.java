package az.demo.NexoraAcademy.dto.cms;

import az.demo.NexoraAcademy.entity.enums.CmsContentType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CmsContentResponse(
        Long id,
        String key,
        CmsContentType type,
        String title,
        String body,
        Map<String, Object> data,
        Boolean published,
        Integer sortOrder,
        UUID updatedBy,
        Instant updatedAt
) {
}
