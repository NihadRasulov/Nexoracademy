using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IUserApiClient
{
    Task<UserResponse> GetMeAsync(CancellationToken ct = default);
    Task<UserResponse> UpdateMeAsync(UpdateProfileRequest request, CancellationToken ct = default);
    Task ChangePasswordAsync(ChangePasswordRequest request, CancellationToken ct = default);

    Task<BackendPage<UserResponse>> ListAsync(
        string? q, string? role, string? status, int? page, int? size, string? sort,
        CancellationToken ct = default);

    Task<UserResponse> GetAsync(Guid id, CancellationToken ct = default);
    Task<UserResponse> CreateAsync(UserRequest request, CancellationToken ct = default);
    Task<UserResponse> ReplaceAsync(Guid id, UserRequest request, CancellationToken ct = default);
    Task<UserResponse> PatchAsync(Guid id, UserRequest request, CancellationToken ct = default);
    Task DeleteAsync(Guid id, CancellationToken ct = default);
}
