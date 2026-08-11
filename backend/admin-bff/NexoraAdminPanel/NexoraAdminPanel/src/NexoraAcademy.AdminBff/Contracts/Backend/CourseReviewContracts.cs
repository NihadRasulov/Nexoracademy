using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record CourseReviewRequest(
    [property: JsonPropertyName("courseId")] Guid? CourseId,
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("enrollmentId")] Guid? EnrollmentId,
    [property: JsonPropertyName("rating")] short? Rating,
    [property: JsonPropertyName("comment")] string? Comment);

public record CourseReviewResponse(
    [property: JsonPropertyName("id")] long Id,
    [property: JsonPropertyName("courseId")] Guid CourseId,
    [property: JsonPropertyName("userId")] Guid UserId,
    [property: JsonPropertyName("enrollmentId")] Guid? EnrollmentId,
    [property: JsonPropertyName("rating")] short Rating,
    [property: JsonPropertyName("comment")] string? Comment,
    [property: JsonPropertyName("published")] bool Published,
    [property: JsonPropertyName("moderatedBy")] Guid? ModeratedBy,
    [property: JsonPropertyName("aiSentiment")] Dictionary<string, object>? AiSentiment,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt);
