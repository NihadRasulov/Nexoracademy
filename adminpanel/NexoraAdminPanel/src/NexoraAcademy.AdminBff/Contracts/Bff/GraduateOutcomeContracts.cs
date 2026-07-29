namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record GraduateOutcomeRequest(
    Guid? UserId, Guid? CourseId, string? CompanyName, string? JobTitle, DateOnly? EmployedAt,
    string? SalaryBand, bool? PublicStory, string? StoryText);

public record GraduateOutcomeResponse(
    long Id, Guid UserId, Guid CourseId, string? CompanyName, string? JobTitle, DateOnly? EmployedAt,
    string? SalaryBand, bool? PublicStory, string? StoryText, DateTimeOffset CreatedAt);
