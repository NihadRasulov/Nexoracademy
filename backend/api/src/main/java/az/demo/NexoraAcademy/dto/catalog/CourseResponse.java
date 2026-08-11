package az.demo.NexoraAcademy.dto.catalog;

import az.demo.NexoraAcademy.entity.enums.DeliveryFormat;
import az.demo.NexoraAcademy.entity.enums.DifficultyLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String slug,
        Short categoryId,
        String title,
        String shortDescription,
        String fullDescription,
        String targetAudience,
        DifficultyLevel difficulty,
        Short durationWeeks,
        DeliveryFormat deliveryFormat,
        String locationText,
        BigDecimal basePrice,
        String currency,
        String pricePeriod,
        Boolean published,
        Boolean active,
        Boolean archived,
        Instant validFrom,
        Instant validUntil,
        Map<String, Object> content,
        UUID[] relatedCourseIds,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
