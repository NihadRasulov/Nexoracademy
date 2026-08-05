namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record CourseReviewRequest(Guid? CourseId, Guid? UserId, Guid? EnrollmentId, short? Rating, string? Comment);

public record CourseReviewResponse(
    long Id, Guid CourseId, Guid UserId, Guid? EnrollmentId, short Rating, string? Comment,
    bool Published, Guid? ModeratedBy, Dictionary<string, object>? AiSentiment, DateTimeOffset CreatedAt);
