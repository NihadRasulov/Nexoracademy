namespace NexoraAcademy.AdminBff.Clients;

public interface IHealthApiClient
{
    Task<string> GetBackendHealthAsync(CancellationToken ct = default);
}

public class HealthApiClient(HttpClient httpClient) : IHealthApiClient
{
    public async Task<string> GetBackendHealthAsync(CancellationToken ct = default)
    {
        using var response = await httpClient.GetAsync("/actuator/health", ct);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadAsStringAsync(ct);
    }
}
