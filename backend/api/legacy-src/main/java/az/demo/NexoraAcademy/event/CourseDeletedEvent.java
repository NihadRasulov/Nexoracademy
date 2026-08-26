package az.demo.NexoraAcademy.event;

import java.util.UUID;

public record CourseDeletedEvent(UUID courseId, String title, UUID deletedBy) {
}
