namespace NexoraAcademy.AdminBff.Clients.Generic;

public interface IBackendCrudClient<TResponse, TRequest, TId>
{
    Task<List<TResponse>> ListAsync(CancellationToken ct = default);
    Task<TResponse> GetAsync(TId id, CancellationToken ct = default);
    Task<TResponse> CreateAsync(TRequest request, CancellationToken ct = default);
    Task<TResponse> ReplaceAsync(TId id, TRequest request, CancellationToken ct = default);
    Task<TResponse> PatchAsync(TId id, TRequest request, CancellationToken ct = default);
    Task DeleteAsync(TId id, CancellationToken ct = default);
}
