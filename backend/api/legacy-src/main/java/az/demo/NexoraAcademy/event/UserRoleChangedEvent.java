package az.demo.NexoraAcademy.event;

import az.demo.NexoraAcademy.entity.enums.UserRole;

import java.util.UUID;

public record UserRoleChangedEvent(UUID userId, UserRole oldRole, UserRole newRole, UUID changedBy) {
}
