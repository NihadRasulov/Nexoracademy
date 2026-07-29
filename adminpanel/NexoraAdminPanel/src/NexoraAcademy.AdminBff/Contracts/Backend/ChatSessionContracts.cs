using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record ChatSessionRequest(
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("leadId")] Guid? LeadId,
    [property: JsonPropertyName("channel")] string? Channel,
    [property: JsonPropertyName("messages")] List<Dictionary<string, object>>? Messages);

public record ChatSessionResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("leadId")] Guid? LeadId,
    [property: JsonPropertyName("channel")] string? Channel,
    [property: JsonPropertyName("messages")] List<Dictionary<string, object>>? Messages,
    [property: JsonPropertyName("startedAt")] DateTimeOffset StartedAt,
    [property: JsonPropertyName("endedAt")] DateTimeOffset? EndedAt);
