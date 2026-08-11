using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[ApiController]
[Route("api/auth")]
[ResponseCache(NoStore = true, Location = ResponseCacheLocation.None)]
public class AuthController(
    IAuthApiClient authApiClient,
    IUserApiClient userApiClient,
    ISessionStore sessionStore,
    ILogger<AuthController> logger) : ControllerBase
{
    [HttpPost("login")]
    [AllowAnonymous]
    [EnableRateLimiting(BffRateLimitPolicies.Login)]
    [RequestSizeLimit(16 * 1024)]
    public async Task<ActionResult<MeResponse>> Login([FromBody] LoginRequest request, CancellationToken ct)
    {
        var response = await authApiClient.LoginAsync(request.Email, request.Password, ct);
        if (response.AccessToken is null)
        {
            throw new OtpRequiredException();
        }

        if (response.RefreshToken is null || response.ExpiresInSeconds is null or <= 0)
        {
            throw new BackendProtocolException(
                "The backend login response is missing required token metadata.");
        }

        var tokenSubject = AccessTokenClaimsReader.ReadSubject(response.AccessToken);

        var session = new BackendSession(
            tokenSubject,
            string.Empty,
            response.AccessToken,
            response.RefreshToken,
            DateTimeOffset.UtcNow.AddSeconds(response.ExpiresInSeconds.Value));

        var sessionId = await sessionStore.CreateAsync(session, ct);

        var provisionalPrincipal = CreatePrincipal(tokenSubject, string.Empty, sessionId);
        HttpContext.User = provisionalPrincipal;

        try
        {
            // Resolve the authoritative backend profile before issuing the browser cookie so a
            // partial login cannot survive a downstream failure or trust an unvalidated JWT role.
            var me = await userApiClient.GetMeAsync(ct);
            var role = me.Role.ToString();

            if (me.Id != tokenSubject)
            {
                throw new BackendProtocolException(
                    "The backend profile does not match the access token subject.");
            }

            if (!Roles.CanAccessPanel(role) || me.Status != Contracts.Backend.AccountStatus.ACTIVE)
            {
                await TryRevokeBackendSessionAsync(response.RefreshToken, ct);
                throw new PanelAccessDeniedException();
            }

            await sessionStore.SetAsync(sessionId, session with { Role = role }, ct);
            var principal = CreatePrincipal(me.Id, role, sessionId);
            HttpContext.User = principal;
            await HttpContext.SignInAsync(BffAuthConstants.CookieScheme, principal);

            logger.LogInformation("User {UserId} logged in with role {Role}.", me.Id, role);
            return Ok(ToMeResponse(me));
        }
        catch
        {
            await sessionStore.RemoveAsync(sessionId, CancellationToken.None);
            throw;
        }
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
                await TryRevokeBackendSessionAsync(session.RefreshToken, ct);
            }

            // Local logout must succeed even when the Java backend is temporarily unavailable.
            await sessionStore.RemoveAsync(sessionId, CancellationToken.None);
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

    private static ClaimsPrincipal CreatePrincipal(Guid userId, string role, string sessionId)
    {
        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, userId.ToString()),
            new(BffAuthConstants.SessionIdClaimType, sessionId)
        };

        if (!string.IsNullOrWhiteSpace(role))
        {
            claims.Add(new Claim(ClaimTypes.Role, role));
        }

        return new ClaimsPrincipal(new ClaimsIdentity(claims, BffAuthConstants.CookieScheme));
    }

    private async Task TryRevokeBackendSessionAsync(string refreshToken, CancellationToken ct)
    {
        try
        {
            await authApiClient.LogoutAsync(refreshToken, ct);
        }
        catch (Exception ex) when (ex is BackendApiException or HttpRequestException or TaskCanceledException)
        {
            logger.LogWarning(ex, "Backend session revocation failed; local logout will continue.");
        }
    }
}
