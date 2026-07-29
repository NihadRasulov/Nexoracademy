using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController(
    IAuthApiClient authApiClient,
    IUserApiClient userApiClient,
    ISessionStore sessionStore,
    ILogger<AuthController> logger) : ControllerBase
{
    [HttpPost("login")]
    [AllowAnonymous]
    public async Task<ActionResult<MeResponse>> Login([FromBody] LoginRequest request, CancellationToken ct)
    {
        var response = await authApiClient.LoginAsync(request.Email, request.Password, ct);
        if (response.AccessToken is null)
        {
            throw new OtpRequiredException(request.Email);
        }

        var (userId, role) = AccessTokenClaimsReader.Read(response.AccessToken);

        var session = new BackendSession(
            userId,
            role,
            response.AccessToken,
            response.RefreshToken!,
            DateTimeOffset.UtcNow.AddSeconds(response.ExpiresInSeconds!.Value));

        var sessionId = await sessionStore.CreateAsync(session, ct);

        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, userId.ToString()),
            new(ClaimTypes.Role, role),
            new(BffAuthConstants.SessionIdClaimType, sessionId)
        };
        var identity = new ClaimsIdentity(claims, BffAuthConstants.CookieScheme);
        var principal = new ClaimsPrincipal(identity);
        await HttpContext.SignInAsync(BffAuthConstants.CookieScheme, principal);
        HttpContext.User = principal;

        logger.LogInformation("User {UserId} logged in with role {Role}.", userId, role);

        var me = await userApiClient.GetMeAsync(ct);
        return Ok(ToMeResponse(me));
    }

    [HttpPost("logout")]
    [Authorize]
    public async Task<IActionResult> Logout(CancellationToken ct)
    {
        var sessionId = User.FindFirst(BffAuthConstants.SessionIdClaimType)?.Value;
        if (sessionId is not null)
        {
            var session = await sessionStore.GetAsync(sessionId, ct);
            if (session is not null)
            {
                await authApiClient.LogoutAsync(session.RefreshToken, ct);
            }
            await sessionStore.RemoveAsync(sessionId, ct);
        }

        await HttpContext.SignOutAsync(BffAuthConstants.CookieScheme);
        return NoContent();
    }

    [HttpGet("me")]
    [Authorize]
    public async Task<ActionResult<MeResponse>> Me(CancellationToken ct)
    {
        var me = await userApiClient.GetMeAsync(ct);
        return Ok(ToMeResponse(me));
    }

    private static MeResponse ToMeResponse(Contracts.Backend.UserResponse u) => new(
        u.Id, u.Email, u.FullName, u.Phone, u.Role.ToString(), u.Status.ToString(), u.Locale, u.LastLoginAt);
}
