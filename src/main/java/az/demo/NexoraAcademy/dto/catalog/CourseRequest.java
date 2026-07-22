package az.demo.NexoraAcademy.dto.catalog;

import az.demo.NexoraAcademy.entity.enums.DeliveryFormat;
import az.demo.NexoraAcademy.entity.enums.DifficultyLevel;
import az.demo.NexoraAcademy.validation.DateRange;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@DateRange(startField = "validFrom", endField = "validUntil", inclusive = true)
public record CourseRequest(
        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 160)
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "slug must be lowercase, alphanumeric, hyphen-separated")
        String slug,

        @NotNull(groups = ValidationGroups.OnCreate.class) Short categoryId,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 3, max = 200) String title,

        @Size(max = 400) String shortDescription,

        @Size(max = 20000) String fullDescription,

        @Size(max = 5000) String targetAudience,

        @NotNull(groups = ValidationGroups.OnCreate.class) DifficultyLevel difficulty,

        @Positive Short durationWeeks,

        @NotNull(groups = ValidationGroups.OnCreate.class) DeliveryFormat deliveryFormat,

        @Size(max = 255) String locationText,

        @DecimalMin(value = "0.0", inclusive = true) BigDecimal basePrice,

        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code") String currency,

        @Size(max = 30) String pricePeriod,

        Boolean published,

        Boolean active,

        Boolean archived,

        Instant validFrom,

        Instant validUntil,

        @Size(max = 100) Map<String, Object> content,

        @Size(max = 20) UUID[] relatedCourseIds
) {
}
