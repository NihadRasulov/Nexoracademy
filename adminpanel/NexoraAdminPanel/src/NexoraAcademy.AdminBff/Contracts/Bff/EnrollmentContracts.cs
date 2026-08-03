namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record EnrollmentRequest(
    Guid? UserId, Guid? GroupId, string? Status, string? IdempotencyKey,
    string? ConsentVersion, DateTimeOffset? ConsentGivenAt);

public record EnrollmentResponse(
    Guid Id, Guid UserId, Guid GroupId, string Status, string IdempotencyKey,
    string? ConsentVersion, DateTimeOffset? ConsentGivenAt, DateTimeOffset? HoldExpiresAt,
    DateTimeOffset EnrolledAt, DateTimeOffset? CompletedAt, DateTimeOffset? CancelledAt, string? CancelReason);

public record CancelEnrollmentRequest(string? Reason);
