namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record CourseInstructorRequest(Guid? CourseId, Guid? InstructorId, string? Role);

public record CourseInstructorResponse(Guid CourseId, Guid InstructorId, string Role);
