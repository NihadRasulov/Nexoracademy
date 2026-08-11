package az.demo.NexoraAcademy.dto.identity;

import az.demo.NexoraAcademy.entity.enums.OAuthProvider;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OAuthAccountRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID userId,

        @NotNull(groups = ValidationGroups.OnCreate.class) OAuthProvider provider,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 255) String providerUserId,

        @Size(max = 4000) String accessTokenEnc,

        @Size(max = 4000) String refreshTokenEnc
) {
}
