using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record SessionRequest(
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("type")] string? Type,
    [property: JsonPropertyName("tokenHash")] string? TokenHash,
    [property: JsonPropertyName("ipAddress")] string? IpAddress,
    [property: JsonPropertyName("userAgent")] string? UserAgent,
    [property: JsonPropertyName("expiresAt")] DateTimeOffset? ExpiresAt);

public record SessionResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("userId")] Guid UserId,
    [property: JsonPropertyName("type")] string Type,
    [property: JsonPropertyName("ipAddress")] string? IpAddress,
    [property: JsonPropertyName("userAgent")] string? UserAgent,
    [property: JsonPropertyName("issuedAt")] DateTimeOffset IssuedAt,
    [property: JsonPropertyName("expiresAt")] DateTimeOffset ExpiresAt,
    [property: JsonPropertyName("usedAt")] DateTimeOffset? UsedAt,
    [property: JsonPropertyName("revokedAt")] DateTimeOffset? RevokedAt);
