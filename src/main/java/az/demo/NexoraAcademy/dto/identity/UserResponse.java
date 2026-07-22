package az.demo.NexoraAcademy.dto.identity;

import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String phone,
        String fullName,
        UserRole role,
        AccountStatus status,
        String locale,
        Map<String, Object> profile,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt
) {
}
