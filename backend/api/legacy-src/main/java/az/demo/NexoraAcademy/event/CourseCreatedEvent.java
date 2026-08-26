package az.demo.NexoraAcademy.event;

import java.util.UUID;

public record CourseCreatedEvent(UUID courseId, String title, UUID createdBy) {
}
