package az.demo.NexoraAcademy.dto.outcomes;

import java.time.Instant;

/** Public reviews never reveal user, enrollment, moderation or AI metadata. */
public record PublicCourseReviewResponse(Short rating, String comment, Instant createdAt) {
}
