using System.Globalization;
using Microsoft.AspNetCore.WebUtilities;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public class CourseApiClient(HttpClient httpClient) : ICourseApiClient
{
    private const string BasePath = "/api/v1/courses";

    public Task<BackendPage<CourseResponse>> ListAsync(
        string? q, short? categoryId, string? difficulty, string? deliveryFormat,
        bool? published, bool? active, int? page, int? size, string? sort, CancellationToken ct = default)
    {
        var query = new Dictionary<string, string?>
        {
            ["q"] = q,
            ["categoryId"] = categoryId?.ToString(CultureInfo.InvariantCulture),
            ["difficulty"] = difficulty,
            ["deliveryFormat"] = deliveryFormat,
            ["published"] = published is null ? null : published.Value ? "true" : "false",
            ["active"] = active is null ? null : active.Value ? "true" : "false",
            ["page"] = page?.ToString(CultureInfo.InvariantCulture),
            ["size"] = size?.ToString(CultureInfo.InvariantCulture),
            ["sort"] = sort
        };
        var nonNull = query.Where(kv => kv.Value is not null).ToDictionary(kv => kv.Key, kv => kv.Value);
        var path = QueryHelpers.AddQueryString(BasePath, nonNull);
        return BackendHttpJson.SendAsync<BackendPage<CourseResponse>>(httpClient, HttpMethod.Get, path, null, ct);
    }

    public Task<CourseResponse> GetAsync(Guid id, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<CourseResponse>(httpClient, HttpMethod.Get, $"{BasePath}/{id}", null, ct);

    public Task<CourseResponse> CreateAsync(CourseRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<CourseResponse>(httpClient, HttpMethod.Post, BasePath, request, ct);

    public Task<CourseResponse> ReplaceAsync(Guid id, CourseRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<CourseResponse>(httpClient, HttpMethod.Put, $"{BasePath}/{id}", request, ct);

    public Task<CourseResponse> PatchAsync(Guid id, CourseRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<CourseResponse>(httpClient, HttpMethod.Patch, $"{BasePath}/{id}", request, ct);

    public Task DeleteAsync(Guid id, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync(httpClient, HttpMethod.Delete, $"{BasePath}/{id}", null, ct);
}
