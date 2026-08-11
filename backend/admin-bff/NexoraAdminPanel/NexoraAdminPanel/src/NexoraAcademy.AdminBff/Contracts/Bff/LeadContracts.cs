namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record LeadRequest(
    string? FullName, string? Email, string? Phone, Guid? CourseId, string? Source,
    Guid? AssignedTo, string? ConsentVersion);

public record LeadResponse(
    Guid Id, string? FullName, string? Email, string? Phone, Guid? CourseId, string Source, string Status,
    Guid? AssignedTo, string? ConsentVersion, DateTimeOffset? ConsentGivenAt, Guid? DuplicateOfLeadId,
    List<Dictionary<string, object>>? ActivityLog, DateTimeOffset CreatedAt, DateTimeOffset UpdatedAt);
