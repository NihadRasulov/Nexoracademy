using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record NotificationRequest(
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("type")] string? Type,
    [property: JsonPropertyName("channel")] string? Channel,
    [property: JsonPropertyName("payload")] Dictionary<string, object>? Payload);

public record NotificationResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("userId")] Guid UserId,
    [property: JsonPropertyName("type")] string Type,
    [property: JsonPropertyName("channel")] string Channel,
    [property: JsonPropertyName("payload")] Dictionary<string, object>? Payload,
    [property: JsonPropertyName("status")] string Status,
    [property: JsonPropertyName("sentAt")] DateTimeOffset? SentAt,
    [property: JsonPropertyName("readAt")] DateTimeOffset? ReadAt,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt);
