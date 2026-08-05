namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record ChatSessionRequest(Guid? UserId, Guid? LeadId, string? Channel, List<Dictionary<string, object>>? Messages);

public record ChatSessionResponse(
    Guid Id, Guid? UserId, Guid? LeadId, string? Channel, List<Dictionary<string, object>>? Messages,
    DateTimeOffset StartedAt, DateTimeOffset? EndedAt);
