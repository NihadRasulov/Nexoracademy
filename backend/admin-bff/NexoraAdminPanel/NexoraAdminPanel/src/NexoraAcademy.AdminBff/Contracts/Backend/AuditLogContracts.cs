using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record AuditLogRequest(
    [property: JsonPropertyName("actorId")] Guid? ActorId,
    [property: JsonPropertyName("action")] string? Action,
    [property: JsonPropertyName("entityType")] string? EntityType,
    [property: JsonPropertyName("entityId")] string? EntityId,
    [property: JsonPropertyName("beforeState")] Dictionary<string, object>? BeforeState,
    [property: JsonPropertyName("afterState")] Dictionary<string, object>? AfterState,
    [property: JsonPropertyName("ipAddress")] string? IpAddress);

public record AuditLogResponse(
    [property: JsonPropertyName("id")] long Id,
    [property: JsonPropertyName("actorId")] Guid? ActorId,
    [property: JsonPropertyName("action")] string Action,
    [property: JsonPropertyName("entityType")] string EntityType,
    [property: JsonPropertyName("entityId")] string EntityId,
    [property: JsonPropertyName("beforeState")] Dictionary<string, object>? BeforeState,
    [property: JsonPropertyName("afterState")] Dictionary<string, object>? AfterState,
    [property: JsonPropertyName("traceId")] Guid? TraceId,
    [property: JsonPropertyName("ipAddress")] string? IpAddress,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt);
