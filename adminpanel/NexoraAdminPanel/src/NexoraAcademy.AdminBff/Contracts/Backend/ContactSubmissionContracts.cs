using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record ContactSubmissionRequest(
    [property: JsonPropertyName("leadId")] Guid? LeadId,
    [property: JsonPropertyName("type")] string? Type,
    [property: JsonPropertyName("courseId")] Guid? CourseId,
    [property: JsonPropertyName("fullName")] string? FullName,
    [property: JsonPropertyName("email")] string? Email,
    [property: JsonPropertyName("phone")] string? Phone,
    [property: JsonPropertyName("message")] string? Message,
    [property: JsonPropertyName("preferredTime")] DateTimeOffset? PreferredTime);

public record ContactSubmissionResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("leadId")] Guid? LeadId,
    [property: JsonPropertyName("type")] string Type,
    [property: JsonPropertyName("courseId")] Guid? CourseId,
    [property: JsonPropertyName("fullName")] string? FullName,
    [property: JsonPropertyName("email")] string? Email,
    [property: JsonPropertyName("phone")] string? Phone,
    [property: JsonPropertyName("message")] string? Message,
    [property: JsonPropertyName("preferredTime")] DateTimeOffset? PreferredTime,
    [property: JsonPropertyName("status")] string Status,
    [property: JsonPropertyName("submittedAt")] DateTimeOffset SubmittedAt);
