package az.demo.NexoraAcademy.dto.auth;

import java.util.UUID;

public record RegisterResponse(UUID userId, String email, String message) {
}
