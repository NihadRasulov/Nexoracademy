using System.Net.Http.Json;
using System.Text.Json;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public static class BackendHttpJson
{
    public static readonly JsonSerializerOptions Options = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public static async Task<TResponse> SendAsync<TResponse>(
        HttpClient client, HttpMethod method, string path, object? body, CancellationToken ct)
    {
        using var request = new HttpRequestMessage(method, path);
        if (body is not null)
        {
            request.Content = JsonContent.Create(body, options: Options);
        }

        using var response = await client.SendAsync(request, ct);
        await EnsureSuccessAsync(response, ct);

        if (response.StatusCode == System.Net.HttpStatusCode.NoContent)
        {
            return default!;
        }

        try
        {
            var result = await response.Content.ReadFromJsonAsync<TResponse>(Options, ct);
            return result ?? throw new BackendProtocolException(
                $"The backend returned an empty JSON response for {method} {path}.");
        }
        catch (JsonException ex)
        {
            throw new BackendProtocolException(
                $"The backend returned malformed JSON for {method} {path}.",
                ex);
        }
    }

    public static async Task SendAsync(
        HttpClient client, HttpMethod method, string path, object? body, CancellationToken ct)
    {
        using var request = new HttpRequestMessage(method, path);
        if (body is not null)
        {
            request.Content = JsonContent.Create(body, options: Options);
        }

        using var response = await client.SendAsync(request, ct);
        await EnsureSuccessAsync(response, ct);
    }

    private static async Task EnsureSuccessAsync(HttpResponseMessage response, CancellationToken ct)
    {
        if (response.IsSuccessStatusCode)
        {
            return;
        }

        BackendErrorResponse? error = null;
        try
        {
            error = await response.Content.ReadFromJsonAsync<BackendErrorResponse>(Options, ct);
        }
        catch (Exception ex) when (ex is JsonException or NotSupportedException)
        {
        }

        throw new BackendApiException((int)response.StatusCode, error);
    }
}
