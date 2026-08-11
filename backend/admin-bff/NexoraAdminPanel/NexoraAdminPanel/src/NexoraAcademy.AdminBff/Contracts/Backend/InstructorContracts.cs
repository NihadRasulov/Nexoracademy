using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record InstructorRequest(
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("fullName")] string? FullName,
    [property: JsonPropertyName("bio")] string? Bio,
    [property: JsonPropertyName("photoUrl")] string? PhotoUrl,
    [property: JsonPropertyName("linkedinUrl")] string? LinkedinUrl,
    [property: JsonPropertyName("certifications")] List<Dictionary<string, object>>? Certifications,
    [property: JsonPropertyName("active")] bool? Active);

public record InstructorResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("userId")] Guid? UserId,
    [property: JsonPropertyName("fullName")] string FullName,
    [property: JsonPropertyName("bio")] string? Bio,
    [property: JsonPropertyName("photoUrl")] string? PhotoUrl,
    [property: JsonPropertyName("linkedinUrl")] string? LinkedinUrl,
    [property: JsonPropertyName("avgRating")] decimal? AvgRating,
    [property: JsonPropertyName("certifications")] List<Dictionary<string, object>>? Certifications,
    [property: JsonPropertyName("active")] bool Active,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt);
