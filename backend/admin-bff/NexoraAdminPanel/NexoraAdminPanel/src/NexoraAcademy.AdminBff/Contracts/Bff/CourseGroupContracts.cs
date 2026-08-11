namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record CourseGroupRequest(
    Guid? CourseId, string? GroupCode, DateOnly? StartDate, DateOnly? EndDate,
    DateTimeOffset? RegistrationDeadline, int? TotalSeats, string? Status,
    List<Dictionary<string, object>>? Schedule);

public record CourseGroupResponse(
    Guid Id, Guid CourseId, string GroupCode, DateOnly StartDate, DateOnly? EndDate,
    DateTimeOffset? RegistrationDeadline, int TotalSeats, int ReservedSeats, string Status,
    List<Dictionary<string, object>>? Schedule, DateTimeOffset CreatedAt);
