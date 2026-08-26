using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IUserApiClient
{
    Task<UserResponse> GetMeAsync(CancellationToken ct = default);
}
