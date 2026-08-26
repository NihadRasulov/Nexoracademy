package az.demo.NexoraAcademy.event;

import java.util.UUID;

public record UserLoggedInEvent(UUID userId, String email, String ipAddress) {
}
