namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record InstructorRequest(
    Guid? UserId, string? FullName, string? Bio, string? PhotoUrl, string? LinkedinUrl,
    List<Dictionary<string, object>>? Certifications, bool? Active);

public record InstructorResponse(
    Guid Id, Guid? UserId, string FullName, string? Bio, string? PhotoUrl, string? LinkedinUrl,
    decimal? AvgRating, List<Dictionary<string, object>>? Certifications, bool Active, DateTimeOffset CreatedAt);
