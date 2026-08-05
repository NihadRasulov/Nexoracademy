namespace NexoraAcademy.AdminBff.Clients.Generic;

public abstract class BackendCrudClient<TResponse, TRequest, TId>(HttpClient httpClient, string basePath)
    : IBackendCrudClient<TResponse, TRequest, TId>
{
    protected HttpClient HttpClient { get; } = httpClient;

    public Task<List<TResponse>> ListAsync(CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<List<TResponse>>(HttpClient, HttpMethod.Get, basePath, null, ct);

    public Task<TResponse> GetAsync(TId id, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<TResponse>(HttpClient, HttpMethod.Get, $"{basePath}/{id}", null, ct);

    public Task<TResponse> CreateAsync(TRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<TResponse>(HttpClient, HttpMethod.Post, basePath, request, ct);

    public Task<TResponse> ReplaceAsync(TId id, TRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<TResponse>(HttpClient, HttpMethod.Put, $"{basePath}/{id}", request, ct);

    public Task<TResponse> PatchAsync(TId id, TRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<TResponse>(HttpClient, HttpMethod.Patch, $"{basePath}/{id}", request, ct);

    public Task DeleteAsync(TId id, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync(HttpClient, HttpMethod.Delete, $"{basePath}/{id}", null, ct);
}
