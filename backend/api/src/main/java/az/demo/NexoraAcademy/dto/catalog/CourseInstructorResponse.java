package az.demo.NexoraAcademy.dto.catalog;

import java.util.UUID;

public record CourseInstructorResponse(
        UUID courseId,
        UUID instructorId,
        String role
) {
}
