using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Clients;

namespace NexoraAcademy.AdminBff.Controllers;

[ApiController]
[Route("api/health")]
[AllowAnonymous]
public class HealthController(IHealthApiClient healthApiClient) : ControllerBase
{
    [HttpGet("backend")]
    public async Task<IActionResult> Backend(CancellationToken ct)
    {
        var body = await healthApiClient.GetBackendHealthAsync(ct);
        return Content(body, "application/json");
    }
}
