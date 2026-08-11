namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record AuditLogRequest(
    Guid? ActorId, string? Action, string? EntityType, string? EntityId,
    Dictionary<string, object>? BeforeState, Dictionary<string, object>? AfterState, string? IpAddress);

public record AuditLogResponse(
    long Id, Guid? ActorId, string Action, string EntityType, string EntityId,
    Dictionary<string, object>? BeforeState, Dictionary<string, object>? AfterState,
    Guid? TraceId, string? IpAddress, DateTimeOffset CreatedAt);
