package az.demo.NexoraAcademy.dto.platform;

import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record AuditLogRequest(
        UUID actorId,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 80)
        @Pattern(regexp = "^[a-z0-9_.]+$", message = "action must be lowercase snake/dot-case (e.g. course.publish)")
        String action,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 60) String entityType,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 255) String entityId,

        @Size(max = 100) Map<String, Object> beforeState,

        @Size(max = 100) Map<String, Object> afterState,

        @Size(max = 45)
        @Pattern(
                regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}|[0-9a-fA-F:]+)$",
                message = "ipAddress must be a valid IPv4 or IPv6 address"
        )
        String ipAddress
) {
}
