using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record EnrollmentRequest(
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("groupId")] Guid? GroupId,
    [property: JsonPropertyName("status")] EnrollmentStatus? Status,
    [property: JsonPropertyName("idempotencyKey")] string? IdempotencyKey,
    [property: JsonPropertyName("consentVersion")] string? ConsentVersion,
    [property: JsonPropertyName("consentGivenAt")] DateTimeOffset? ConsentGivenAt);

public record EnrollmentResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("userId")] Guid UserId,
    [property: JsonPropertyName("groupId")] Guid GroupId,
    [property: JsonPropertyName("status")] EnrollmentStatus Status,
    [property: JsonPropertyName("idempotencyKey")] string IdempotencyKey,
    [property: JsonPropertyName("consentVersion")] string? ConsentVersion,
    [property: JsonPropertyName("consentGivenAt")] DateTimeOffset? ConsentGivenAt,
    [property: JsonPropertyName("holdExpiresAt")] DateTimeOffset? HoldExpiresAt,
    [property: JsonPropertyName("enrolledAt")] DateTimeOffset EnrolledAt,
    [property: JsonPropertyName("completedAt")] DateTimeOffset? CompletedAt,
    [property: JsonPropertyName("cancelledAt")] DateTimeOffset? CancelledAt,
    [property: JsonPropertyName("cancelReason")] string? CancelReason);

public record CancelEnrollmentRequest(
    [property: JsonPropertyName("reason")] string? Reason);
