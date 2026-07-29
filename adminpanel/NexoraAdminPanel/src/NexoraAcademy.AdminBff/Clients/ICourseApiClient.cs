using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ICourseApiClient
{
    Task<BackendPage<CourseResponse>> ListAsync(
        string? q, short? categoryId, string? difficulty, string? deliveryFormat,
        bool? published, bool? active, int? page, int? size, string? sort, CancellationToken ct = default);

    Task<CourseResponse> GetAsync(Guid id, CancellationToken ct = default);
    Task<CourseResponse> CreateAsync(CourseRequest request, CancellationToken ct = default);
    Task<CourseResponse> ReplaceAsync(Guid id, CourseRequest request, CancellationToken ct = default);
    Task<CourseResponse> PatchAsync(Guid id, CourseRequest request, CancellationToken ct = default);
    Task DeleteAsync(Guid id, CancellationToken ct = default);
}
