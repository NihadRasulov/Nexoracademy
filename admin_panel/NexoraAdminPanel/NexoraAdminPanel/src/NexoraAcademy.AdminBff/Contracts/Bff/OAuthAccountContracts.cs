namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record OAuthAccountRequest(
    Guid? UserId, string? Provider, string? ProviderUserId, string? AccessTokenEnc, string? RefreshTokenEnc);

public record OAuthAccountResponse(long Id, Guid UserId, string Provider, string ProviderUserId, DateTimeOffset LinkedAt);
