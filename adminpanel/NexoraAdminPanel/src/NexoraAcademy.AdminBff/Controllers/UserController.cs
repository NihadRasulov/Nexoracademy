using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[ApiController]
[Route("api/admin/users")]
[Authorize]
public class UserController(IUserApiClient client) : ControllerBase
{
    [HttpPatch("me")]
    public async Task<ActionResult<BffC.UserResponse>> UpdateMe(
        [FromBody] BffC.UpdateProfileRequest request, CancellationToken ct)
    {
        var updated = await client.UpdateMeAsync(
            new BackendC.UpdateProfileRequest(request.Email, request.Phone, request.FullName, request.Locale, request.Profile), ct);
        return Ok(ToBff(updated));
    }

    [HttpPost("me/password")]
    public async Task<IActionResult> ChangePassword(
        [FromBody] BffC.ChangePasswordRequest request, CancellationToken ct)
    {
        await client.ChangePasswordAsync(
            new BackendC.ChangePasswordRequest(request.CurrentPassword, request.NewPassword), ct);
        return NoContent();
    }

    [HttpGet]
    [Authorize(Roles = Roles.AdminOnly)]
    public async Task<ActionResult<BffC.PagedResult<BffC.UserResponse>>> List(
        [FromQuery] string? q, [FromQuery] string? role, [FromQuery] string? status,
        [FromQuery] int? page, [FromQuery] int? size, [FromQuery] string? sort, CancellationToken ct)
    {
        var result = await client.ListAsync(q, role, status, page, size, sort, ct);
        return Ok(new BffC.PagedResult<BffC.UserResponse>(
            result.Content.Select(ToBff).ToList(), result.TotalElements, result.TotalPages, result.Number, result.Size));
    }

    [HttpGet("{id:guid}")]
    [Authorize(Roles = Roles.AdminOnly)]
    public async Task<ActionResult<BffC.UserResponse>> Get(Guid id, CancellationToken ct)
    {
        var user = await client.GetAsync(id, ct);
        return Ok(ToBff(user));
    }

    [HttpPost]
    [Authorize(Roles = Roles.AdminOnly)]
    public async Task<ActionResult<BffC.UserResponse>> Create([FromBody] BffC.UserRequest request, CancellationToken ct)
    {
        var created = await client.CreateAsync(ToBackend(request), ct);
        return StatusCode(StatusCodes.Status201Created, ToBff(created));
    }

    [HttpPut("{id:guid}")]
    [Authorize(Roles = Roles.AdminOnly)]
    public async Task<ActionResult<BffC.UserResponse>> Replace(Guid id, [FromBody] BffC.UserRequest request, CancellationToken ct)
    {
        var updated = await client.ReplaceAsync(id, ToBackend(request), ct);
        return Ok(ToBff(updated));
    }

    [HttpPatch("{id:guid}")]
    [Authorize(Roles = Roles.AdminOnly)]
    public async Task<ActionResult<BffC.UserResponse>> Patch(Guid id, [FromBody] BffC.UserRequest request, CancellationToken ct)
    {
        var updated = await client.PatchAsync(id, ToBackend(request), ct);
        return Ok(ToBff(updated));
    }

    [HttpDelete("{id:guid}")]
    [Authorize(Roles = Roles.AdminOnly)]
    public async Task<IActionResult> Delete(Guid id, CancellationToken ct)
    {
        await client.DeleteAsync(id, ct);
        return NoContent();
    }

    private static BffC.UserResponse ToBff(BackendC.UserResponse u) => new(
        u.Id, u.Email, u.Phone, u.FullName, u.Role.ToString(), u.Status.ToString(), u.Locale,
        u.Profile, u.LastLoginAt, u.CreatedAt, u.UpdatedAt);

    private static BackendC.UserRequest ToBackend(BffC.UserRequest r) => new(
        r.Email, r.Phone, r.FullName, r.Password,
        r.Role is null ? null : Enum.Parse<BackendC.UserRole>(r.Role),
        r.Status is null ? null : Enum.Parse<BackendC.AccountStatus>(r.Status),
        r.Locale, r.Profile);
}
