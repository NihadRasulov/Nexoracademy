using Microsoft.AspNetCore.WebUtilities;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public class UserApiClient(HttpClient httpClient) : IUserApiClient
{
    private const string BasePath = "/api/v1/users";

    public Task<UserResponse> GetMeAsync(CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<UserResponse>(
            httpClient, HttpMethod.Get, $"{BasePath}/me", body: null, ct);

    public Task<UserResponse> UpdateMeAsync(UpdateProfileRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<UserResponse>(
            httpClient, HttpMethod.Patch, $"{BasePath}/me", request, ct);

    public Task ChangePasswordAsync(ChangePasswordRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync(
            httpClient, HttpMethod.Post, $"{BasePath}/me/password", request, ct);

    public Task<BackendPage<UserResponse>> ListAsync(
        string? q, string? role, string? status, int? page, int? size, string? sort,
        CancellationToken ct = default)
    {
        var query = new Dictionary<string, string?>
        {
            ["q"] = q,
            ["role"] = role,
            ["status"] = status,
            ["page"] = page?.ToString(),
            ["size"] = size?.ToString(),
            ["sort"] = sort
        };
        var nonNull = query.Where(kv => kv.Value is not null).ToDictionary(kv => kv.Key, kv => kv.Value);
        var path = QueryHelpers.AddQueryString(BasePath, nonNull);
        return BackendHttpJson.SendAsync<BackendPage<UserResponse>>(httpClient, HttpMethod.Get, path, null, ct);
    }

    public Task<UserResponse> GetAsync(Guid id, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<UserResponse>(httpClient, HttpMethod.Get, $"{BasePath}/{id}", null, ct);

    public Task<UserResponse> CreateAsync(UserRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<UserResponse>(httpClient, HttpMethod.Post, BasePath, request, ct);

    public Task<UserResponse> ReplaceAsync(Guid id, UserRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<UserResponse>(httpClient, HttpMethod.Put, $"{BasePath}/{id}", request, ct);

    public Task<UserResponse> PatchAsync(Guid id, UserRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<UserResponse>(httpClient, HttpMethod.Patch, $"{BasePath}/{id}", request, ct);

    public Task DeleteAsync(Guid id, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync(httpClient, HttpMethod.Delete, $"{BasePath}/{id}", null, ct);
}
