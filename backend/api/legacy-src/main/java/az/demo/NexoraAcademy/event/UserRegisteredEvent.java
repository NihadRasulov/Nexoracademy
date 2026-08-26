package az.demo.NexoraAcademy.event;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String email) {
}
