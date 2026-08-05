namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record SessionRequest(
    Guid? UserId, string? Type, string? TokenHash, string? IpAddress, string? UserAgent, DateTimeOffset? ExpiresAt);

public record SessionResponse(
    Guid Id, Guid UserId, string Type, string? IpAddress, string? UserAgent,
    DateTimeOffset IssuedAt, DateTimeOffset ExpiresAt, DateTimeOffset? UsedAt, DateTimeOffset? RevokedAt);
