package az.demo.NexoraAcademy.dto.outcomes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CourseReviewResponse(
        Long id,
        UUID courseId,
        UUID userId,
        UUID enrollmentId,
        Short rating,
        String comment,
        Boolean published,
        UUID moderatedBy,
        Map<String, Object> aiSentiment,
        Instant createdAt
) {
}
