using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ICourseInstructorApiClient
{
    Task<List<CourseInstructorResponse>> ListAsync(CancellationToken ct = default);
    Task<CourseInstructorResponse> GetAsync(Guid courseId, Guid instructorId, CancellationToken ct = default);
    Task<CourseInstructorResponse> CreateAsync(CourseInstructorRequest request, CancellationToken ct = default);
    Task<CourseInstructorResponse> ReplaceAsync(Guid courseId, Guid instructorId, CourseInstructorRequest request, CancellationToken ct = default);
    Task<CourseInstructorResponse> PatchAsync(Guid courseId, Guid instructorId, CourseInstructorRequest request, CancellationToken ct = default);
    Task DeleteAsync(Guid courseId, Guid instructorId, CancellationToken ct = default);
}
