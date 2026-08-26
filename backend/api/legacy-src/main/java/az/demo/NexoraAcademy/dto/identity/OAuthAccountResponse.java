package az.demo.NexoraAcademy.dto.identity;

import az.demo.NexoraAcademy.entity.enums.OAuthProvider;

import java.time.Instant;
import java.util.UUID;

public record OAuthAccountResponse(
        Long id,
        UUID userId,
        OAuthProvider provider,
        String providerUserId,
        Instant linkedAt
) {
}
