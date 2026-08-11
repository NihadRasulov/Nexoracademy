package az.demo.NexoraAcademy.dto.academics;

import az.demo.NexoraAcademy.entity.enums.GroupStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CourseGroupResponse(
        UUID id,
        UUID courseId,
        String groupCode,
        LocalDate startDate,
        LocalDate endDate,
        Instant registrationDeadline,
        Integer totalSeats,
        Integer reservedSeats,
        GroupStatus status,
        List<Map<String, Object>> schedule,
        Instant createdAt
) {
}
