using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record CourseInstructorRequest(
    [property: JsonPropertyName("courseId")] Guid? CourseId,
    [property: JsonPropertyName("instructorId")] Guid? InstructorId,
    [property: JsonPropertyName("role")] string? Role);

public record CourseInstructorResponse(
    [property: JsonPropertyName("courseId")] Guid CourseId,
    [property: JsonPropertyName("instructorId")] Guid InstructorId,
    [property: JsonPropertyName("role")] string Role);
