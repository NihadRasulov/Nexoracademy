package az.demo.NexoraAcademy.dto.outcomes;

import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CourseReviewRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID courseId,

        @NotNull(groups = ValidationGroups.OnCreate.class) UUID userId,

        UUID enrollmentId,

        @NotNull(groups = ValidationGroups.OnCreate.class) @Min(1) @Max(5) Short rating,

        @Size(max = 4000) String comment
) {
}
