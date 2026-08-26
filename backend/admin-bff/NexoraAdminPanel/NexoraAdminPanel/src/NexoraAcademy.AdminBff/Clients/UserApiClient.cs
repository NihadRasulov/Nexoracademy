using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public class UserApiClient(HttpClient httpClient) : IUserApiClient
{
    private const string BasePath = "/api/v1/users";

    public Task<UserResponse> GetMeAsync(CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<UserResponse>(
            httpClient, HttpMethod.Get, $"{BasePath}/me", body: null, ct);
}
