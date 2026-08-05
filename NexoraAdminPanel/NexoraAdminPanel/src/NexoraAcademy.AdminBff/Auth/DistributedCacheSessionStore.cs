using System.Security.Cryptography;
using System.Text.Json;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.Extensions.Caching.Distributed;
using NexoraAcademy.AdminBff.Configuration;

namespace NexoraAcademy.AdminBff.Auth;

public class DistributedCacheSessionStore : ISessionStore
{
    private const string KeyPrefix = "bff-session:";
    private const string DataProtectionPurpose = "NexoraAcademy.AdminBff.BackendSession.v1";

    private readonly IDistributedCache cache;
    private readonly IDataProtector protector;
    private readonly ILogger<DistributedCacheSessionStore> logger;
    private readonly TimeSpan sessionTtl;

    public DistributedCacheSessionStore(
        IDistributedCache cache,
        IDataProtectionProvider dataProtectionProvider,
        AdminSettings settings,
        ILogger<DistributedCacheSessionStore> logger)
    {
        this.cache = cache;
        this.logger = logger;
        protector = dataProtectionProvider.CreateProtector(DataProtectionPurpose);
        sessionTtl = TimeSpan.FromMinutes(settings.SessionIdleTimeoutMinutes);
    }

    public async Task<string> CreateAsync(BackendSession session, CancellationToken ct = default)
    {
        var sessionId = Guid.NewGuid().ToString("N");
        await SetAsync(sessionId, session, ct);
        return sessionId;
    }

    public async Task<BackendSession?> GetAsync(string sessionId, CancellationToken ct = default)
    {
        var protectedPayload = await cache.GetStringAsync(KeyPrefix + sessionId, ct);
        if (protectedPayload is null)
        {
            return null;
        }

        try
        {
            var json = protector.Unprotect(protectedPayload);
            return JsonSerializer.Deserialize<BackendSession>(json);
        }
        catch (Exception ex) when (ex is CryptographicException or JsonException)
        {
            // Treat unreadable cache entries as expired sessions. This prevents corrupted or
            // key-ring-incompatible token payloads from reaching outbound API requests.
            logger.LogWarning(ex, "An unreadable admin session was removed from the cache.");
            await cache.RemoveAsync(KeyPrefix + sessionId, ct);
            return null;
        }
    }

    public Task SetAsync(string sessionId, BackendSession session, CancellationToken ct = default)
    {
        var json = JsonSerializer.Serialize(session);
        var protectedPayload = protector.Protect(json);
        return cache.SetStringAsync(
            KeyPrefix + sessionId,
            protectedPayload,
            new DistributedCacheEntryOptions { SlidingExpiration = sessionTtl },
            ct);
    }

    public Task RemoveAsync(string sessionId, CancellationToken ct = default) =>
        cache.RemoveAsync(KeyPrefix + sessionId, ct);
}
