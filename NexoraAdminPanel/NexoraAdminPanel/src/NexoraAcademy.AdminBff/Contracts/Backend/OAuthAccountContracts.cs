using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record OAuthAccountRequest(
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("provider")] string? Provider,
    [property: JsonPropertyName("providerUserId")] string? ProviderUserId,
    [property: JsonPropertyName("accessTokenEnc")] string? AccessTokenEnc,
    [property: JsonPropertyName("refreshTokenEnc")] string? RefreshTokenEnc);

public record OAuthAccountResponse(
    [property: JsonPropertyName("id")] long Id,
    [property: JsonPropertyName("userId")] Guid UserId,
    [property: JsonPropertyName("provider")] string Provider,
    [property: JsonPropertyName("providerUserId")] string ProviderUserId,
    [property: JsonPropertyName("linkedAt")] DateTimeOffset LinkedAt);
