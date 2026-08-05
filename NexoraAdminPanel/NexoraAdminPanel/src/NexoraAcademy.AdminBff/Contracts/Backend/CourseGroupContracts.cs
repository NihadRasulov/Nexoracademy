using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record CourseGroupRequest(
    [property: JsonPropertyName("courseId")] Guid? CourseId,
    [property: JsonPropertyName("groupCode")] string? GroupCode,
    [property: JsonPropertyName("startDate")] DateOnly? StartDate,
    [property: JsonPropertyName("endDate")] DateOnly? EndDate,
    [property: JsonPropertyName("registrationDeadline")] DateTimeOffset? RegistrationDeadline,
    [property: JsonPropertyName("totalSeats")] int? TotalSeats,
    [property: JsonPropertyName("status")] string? Status,
    [property: JsonPropertyName("schedule")] List<Dictionary<string, object>>? Schedule);

public record CourseGroupResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("courseId")] Guid CourseId,
    [property: JsonPropertyName("groupCode")] string GroupCode,
    [property: JsonPropertyName("startDate")] DateOnly StartDate,
    [property: JsonPropertyName("endDate")] DateOnly? EndDate,
    [property: JsonPropertyName("registrationDeadline")] DateTimeOffset? RegistrationDeadline,
    [property: JsonPropertyName("totalSeats")] int TotalSeats,
    [property: JsonPropertyName("reservedSeats")] int ReservedSeats,
    [property: JsonPropertyName("status")] string Status,
    [property: JsonPropertyName("schedule")] List<Dictionary<string, object>>? Schedule,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt);
