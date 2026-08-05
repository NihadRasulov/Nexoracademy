namespace NexoraAcademy.AdminBff.Auth;

public record BackendSession(
    Guid UserId,
    string Role,
    string AccessToken,
    string RefreshToken,
    DateTimeOffset AccessTokenExpiresAt)
{
    public BackendSession WithTokens(string accessToken, string refreshToken, DateTimeOffset accessTokenExpiresAt) =>
        this with
        {
            AccessToken = accessToken,
            RefreshToken = refreshToken,
            AccessTokenExpiresAt = accessTokenExpiresAt
        };
}
