namespace NexoraAcademy.AdminBff.Auth;

public interface ISessionStore
{
    Task<string> CreateAsync(BackendSession session, CancellationToken ct = default);
    Task<BackendSession?> GetAsync(string sessionId, CancellationToken ct = default);
    Task SetAsync(string sessionId, BackendSession session, CancellationToken ct = default);
    Task RemoveAsync(string sessionId, CancellationToken ct = default);
}
