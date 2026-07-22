package az.demo.NexoraAcademy.dto.identity;

import az.demo.NexoraAcademy.entity.enums.SessionType;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record SessionRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID userId,

        SessionType type,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 16, max = 255)
        @Pattern(regexp = "^[A-Za-z0-9+/=_-]+$", message = "tokenHash must be a base64/hex-like string")
        String tokenHash,

        @Size(max = 45)
        @Pattern(
                regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}|[0-9a-fA-F:]+)$",
                message = "ipAddress must be a valid IPv4 or IPv6 address"
        )
        String ipAddress,

        @Size(max = 1000) String userAgent,

        @NotNull(groups = ValidationGroups.OnCreate.class) @Future Instant expiresAt
) {
}
