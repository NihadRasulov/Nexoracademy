using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record GraduateOutcomeRequest(
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("courseId")] Guid? CourseId,
    [property: JsonPropertyName("companyName")] string? CompanyName,
    [property: JsonPropertyName("jobTitle")] string? JobTitle,
    [property: JsonPropertyName("employedAt")] DateOnly? EmployedAt,
    [property: JsonPropertyName("salaryBand")] string? SalaryBand,
    [property: JsonPropertyName("publicStory")] bool? PublicStory,
    [property: JsonPropertyName("storyText")] string? StoryText);

public record GraduateOutcomeResponse(
    [property: JsonPropertyName("id")] long Id,
    [property: JsonPropertyName("userId")] Guid UserId,
    [property: JsonPropertyName("courseId")] Guid CourseId,
    [property: JsonPropertyName("companyName")] string? CompanyName,
    [property: JsonPropertyName("jobTitle")] string? JobTitle,
    [property: JsonPropertyName("employedAt")] DateOnly? EmployedAt,
    [property: JsonPropertyName("salaryBand")] string? SalaryBand,
    [property: JsonPropertyName("publicStory")] bool? PublicStory,
    [property: JsonPropertyName("storyText")] string? StoryText,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt);
