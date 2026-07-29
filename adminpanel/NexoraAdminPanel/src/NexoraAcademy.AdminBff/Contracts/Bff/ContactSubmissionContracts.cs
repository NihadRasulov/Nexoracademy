namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record ContactSubmissionRequest(
    Guid? LeadId, string? Type, Guid? CourseId, string? FullName, string? Email,
    string? Phone, string? Message, DateTimeOffset? PreferredTime);

public record ContactSubmissionResponse(
    Guid Id, Guid? LeadId, string Type, Guid? CourseId, string? FullName, string? Email,
    string? Phone, string? Message, DateTimeOffset? PreferredTime, string Status, DateTimeOffset SubmittedAt);
