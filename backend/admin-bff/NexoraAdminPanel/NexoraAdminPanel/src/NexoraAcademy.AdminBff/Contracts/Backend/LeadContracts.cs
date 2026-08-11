using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record LeadRequest(
    [property: JsonPropertyName("fullName")] string? FullName,
    [property: JsonPropertyName("email")] string? Email,
    [property: JsonPropertyName("phone")] string? Phone,
    [property: JsonPropertyName("courseId")] Guid? CourseId,
    [property: JsonPropertyName("source")] string? Source,
    [property: JsonPropertyName("assignedTo")] Guid? AssignedTo,
    [property: JsonPropertyName("consentVersion")] string? ConsentVersion);

public record LeadResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("fullName")] string? FullName,
    [property: JsonPropertyName("email")] string? Email,
    [property: JsonPropertyName("phone")] string? Phone,
    [property: JsonPropertyName("courseId")] Guid? CourseId,
    [property: JsonPropertyName("source")] string Source,
    [property: JsonPropertyName("status")] string Status,
    [property: JsonPropertyName("assignedTo")] Guid? AssignedTo,
    [property: JsonPropertyName("consentVersion")] string? ConsentVersion,
    [property: JsonPropertyName("consentGivenAt")] DateTimeOffset? ConsentGivenAt,
    [property: JsonPropertyName("duplicateOfLeadId")] Guid? DuplicateOfLeadId,
    [property: JsonPropertyName("activityLog")] List<Dictionary<string, object>>? ActivityLog,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt,
    [property: JsonPropertyName("updatedAt")] DateTimeOffset UpdatedAt);
