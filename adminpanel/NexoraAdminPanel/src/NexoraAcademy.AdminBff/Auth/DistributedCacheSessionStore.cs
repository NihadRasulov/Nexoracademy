using System.Text.Json;
using Microsoft.Extensions.Caching.Distributed;

namespace NexoraAcademy.AdminBff.Auth;

public class DistributedCacheSessionStore(IDistributedCache cache) : ISessionStore
{
    private const string KeyPrefix = "bff-session:";

    private static readonly TimeSpan SessionTtl = TimeSpan.FromDays(30);

    public async Task<string> CreateAsync(BackendSession session, CancellationToken ct = default)
    {
        var sessionId = Guid.NewGuid().ToString("N");
        await SetAsync(sessionId, session, ct);
        return sessionId;
    }

    public async Task<BackendSession?> GetAsync(string sessionId, CancellationToken ct = default)
    {
        var json = await cache.GetStringAsync(KeyPrefix + sessionId, ct);
        return json is null ? null : JsonSerializer.Deserialize<BackendSession>(json);
    }

    public Task SetAsync(string sessionId, BackendSession session, CancellationToken ct = default)
    {
        var json = JsonSerializer.Serialize(session);
        return cache.SetStringAsync(
            KeyPrefix + sessionId,
            json,
            new DistributedCacheEntryOptions { SlidingExpiration = SessionTtl },
            ct);
    }

    public Task RemoveAsync(string sessionId, CancellationToken ct = default) =>
        cache.RemoveAsync(KeyPrefix + sessionId, ct);
}
