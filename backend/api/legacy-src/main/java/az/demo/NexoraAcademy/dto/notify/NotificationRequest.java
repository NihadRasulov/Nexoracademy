package az.demo.NexoraAcademy.dto.notify;

import az.demo.NexoraAcademy.entity.enums.NotificationChannel;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record NotificationRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID userId,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 60)
        @Pattern(regexp = "^[a-z0-9_]+$", message = "type must be lowercase snake_case (e.g. enrollment_confirmed)")
        String type,

        @NotNull(groups = ValidationGroups.OnCreate.class) NotificationChannel channel,

        @Size(max = 50) Map<String, Object> payload
) {
}
