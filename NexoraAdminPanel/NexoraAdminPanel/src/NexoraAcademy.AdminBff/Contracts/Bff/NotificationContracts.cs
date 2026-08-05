namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record NotificationRequest(Guid? UserId, string? Type, string? Channel, Dictionary<string, object>? Payload);

public record NotificationResponse(
    Guid Id, Guid UserId, string Type, string Channel, Dictionary<string, object>? Payload,
    string Status, DateTimeOffset? SentAt, DateTimeOffset? ReadAt, DateTimeOffset CreatedAt);
