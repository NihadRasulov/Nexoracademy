package az.demo.NexoraAcademy.dto.academics;

import az.demo.NexoraAcademy.entity.enums.GroupStatus;
import az.demo.NexoraAcademy.validation.DateRange;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@DateRange(startField = "startDate", endField = "endDate", inclusive = true)
public record CourseGroupRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID courseId,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 40)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "groupCode must be alphanumeric (with - or _)")
        String groupCode,

        @NotNull(groups = ValidationGroups.OnCreate.class) LocalDate startDate,

        LocalDate endDate,

        Instant registrationDeadline,

        @NotNull(groups = ValidationGroups.OnCreate.class) @Positive @Max(10000) Integer totalSeats,

        GroupStatus status,

        @Size(max = 20) List<Map<String, Object>> schedule
) {
}
