using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public class CourseInstructorApiClient(HttpClient httpClient) : ICourseInstructorApiClient
{
    private const string BasePath = "/api/v1/course-instructors";

    public Task<List<CourseInstructorResponse>> ListAsync(CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<List<CourseInstructorResponse>>(httpClient, HttpMethod.Get, BasePath, null, ct);

    public Task<CourseInstructorResponse> GetAsync(Guid courseId, Guid instructorId, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<CourseInstructorResponse>(
            httpClient, HttpMethod.Get, $"{BasePath}/{courseId}/{instructorId}", null, ct);

    public Task<CourseInstructorResponse> CreateAsync(CourseInstructorRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<CourseInstructorResponse>(httpClient, HttpMethod.Post, BasePath, request, ct);

    public Task<CourseInstructorResponse> ReplaceAsync(
        Guid courseId, Guid instructorId, CourseInstructorRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<CourseInstructorResponse>(
            httpClient, HttpMethod.Put, $"{BasePath}/{courseId}/{instructorId}", request, ct);

    public Task<CourseInstructorResponse> PatchAsync(
        Guid courseId, Guid instructorId, CourseInstructorRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<CourseInstructorResponse>(
            httpClient, HttpMethod.Patch, $"{BasePath}/{courseId}/{instructorId}", request, ct);

    public Task DeleteAsync(Guid courseId, Guid instructorId, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync(httpClient, HttpMethod.Delete, $"{BasePath}/{courseId}/{instructorId}", null, ct);
}
