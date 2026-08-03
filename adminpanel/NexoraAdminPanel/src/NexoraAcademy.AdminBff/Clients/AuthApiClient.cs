using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public class AuthApiClient(HttpClient httpClient) : IAuthApiClient
{
    public Task<LoginResponse> LoginAsync(string email, string password, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<LoginResponse>(
            httpClient, HttpMethod.Post, "/api/v1/auth/login",
            new LoginRequest(email, password), ct);

    public Task<TokenResponse> RefreshAsync(string refreshToken, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<TokenResponse>(
            httpClient, HttpMethod.Post, "/api/v1/auth/refresh",
            new RefreshTokenRequest(refreshToken), ct);

    public Task LogoutAsync(string refreshToken, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync(
            httpClient, HttpMethod.Post, "/api/v1/auth/logout",
            new RefreshTokenRequest(refreshToken), ct);
}
