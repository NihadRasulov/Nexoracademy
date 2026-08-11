package az.demo.NexoraAcademy.dto.cms;

import az.demo.NexoraAcademy.entity.enums.CmsContentType;

import java.time.Instant;
import java.util.Map;

/** Public projection: deliberately excludes editor identity and draft state. */
public record PublicCmsContentResponse(
        String key, CmsContentType type, String title, String body,
        Map<String, Object> data, Integer sortOrder, Instant updatedAt) {
}
