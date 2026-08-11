package az.demo.NexoraAcademy.event;

import java.util.UUID;

public record EnrollmentConfirmedEvent(UUID enrollmentId, UUID userId, UUID groupId) {
}
