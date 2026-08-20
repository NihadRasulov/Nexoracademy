using System.Net;
using System.Net.Http.Headers;
using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Contracts.Backend;
using NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Middleware;

public sealed class ApiV1ProxyMiddleware(
    RequestDelegate next,
    IHttpClientFactory httpClientFactory,
    ISessionStore sessionStore,
    IAuthApiClient authApiClient,
    IUserApiClient userApiClient,
    ILogger<ApiV1ProxyMiddleware> logger)
{
    public const string HttpClientName = "NexoraApi-Proxy";

    public async Task InvokeAsync(HttpContext context)
    {
        var path = context.Request.Path.Value;
        if (string.IsNullOrEmpty(path) || !path.StartsWith("/api/v1/", StringComparison.OrdinalIgnoreCase))
        {
            await next(context);
            return;
        }

        if (path.Equals("/api/v1/auth/login", StringComparison.OrdinalIgnoreCase)
            && HttpMethods.IsPost(context.Request.Method))
        {
            await HandleLoginAsync(context);
            return;
        }

        if (path.Equals("/api/v1/auth/logout", StringComparison.OrdinalIgnoreCase)
            && HttpMethods.IsPost(context.Request.Method))
        {
            await HandleLogoutAsync(context);
            return;
        }

        var sessionId = context.User.FindFirst(BffAuthConstants.SessionIdClaimType)?.Value;
        if (string.IsNullOrEmpty(sessionId))
        {
            await WriteErrorAsync(context.Response, StatusCodes.Status401Unauthorized,
                "UNAUTHORIZED", "Daxil olmaq teleb olunur.");
            return;
        }

        var session = await sessionStore.GetAsync(sessionId, context.RequestAborted);
        if (session is null)
        {
            await WriteErrorAsync(context.Response, StatusCodes.Status401Unauthorized,
                "SESSION_EXPIRED", "Sessiya bitib. Yeniden daxil olun.");
            return;
        }

        await ProxyAsync(context, session, sessionId);
    }

    private async Task HandleLoginAsync(HttpContext context)
    {
        context.Request.EnableBuffering();
        Contracts.Bff.LoginRequest? request;
        try
        {
            using var reader = new StreamReader(context.Request.Body, leaveOpen: true);
            var json = await reader.ReadToEndAsync(context.RequestAborted);
            request = string.IsNullOrWhiteSpace(json)
                ? null
                : System.Text.Json.JsonSerializer.Deserialize<Contracts.Bff.LoginRequest>(json,
                    new System.Text.Json.JsonSerializerOptions { PropertyNameCaseInsensitive = true });
            context.Request.Body.Position = 0;
        }
        catch
        {
            await WriteErrorAsync(context.Response, StatusCodes.Status400BadRequest,
                "VALIDATION_ERROR", "Yanlish sorğu formatı.");
            return;
        }

        if (request is null || string.IsNullOrWhiteSpace(request.Email) || string.IsNullOrWhiteSpace(request.Password))
        {
            await WriteErrorAsync(context.Response, StatusCodes.Status400BadRequest,
                "VALIDATION_ERROR", "Email və parol tələb olunur.");
            return;
        }

        var response = await authApiClient.LoginAsync(request.Email, request.Password, context.RequestAborted);

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

        var sessionId = await sessionStore.CreateAsync(session, context.RequestAborted);

        var provisionalPrincipal = CreatePrincipal(tokenSubject, string.Empty, sessionId);
        context.User = provisionalPrincipal;

        try
        {
            var me = await userApiClient.GetMeAsync(context.RequestAborted);
            var role = me.Role.ToString();

            if (me.Id != tokenSubject)
            {
                throw new BackendProtocolException(
                    "The backend profile does not match the access token subject.");
            }

            if (!Roles.CanAccessPanel(role) || me.Status != AccountStatus.ACTIVE)
            {
                await TryRevokeAsync(response.RefreshToken, context.RequestAborted);
                throw new PanelAccessDeniedException();
            }

            await sessionStore.SetAsync(sessionId, session with { Role = role }, context.RequestAborted);
            var principal = CreatePrincipal(me.Id, role, sessionId);
            context.User = principal;
            await context.SignInAsync(BffAuthConstants.CookieScheme, principal);

            logger.LogInformation("User {UserId} logged in via /api/v1/auth/login with role {Role}.", me.Id, role);

            context.Response.StatusCode = StatusCodes.Status200OK;
            context.Response.ContentType = "application/json";
            await context.Response.WriteAsJsonAsync(
                new MeResponse(me.Id, me.Email, me.FullName, me.Phone,
                    me.Role.ToString(), me.Status.ToString(), me.Locale, me.LastLoginAt),
                context.RequestAborted);
        }
        catch
        {
            await sessionStore.RemoveAsync(sessionId, CancellationToken.None);
            throw;
        }
    }

    private async Task HandleLogoutAsync(HttpContext context)
    {
        var sessionId = context.User.FindFirst(BffAuthConstants.SessionIdClaimType)?.Value;
        if (sessionId is not null)
        {
            var session = await sessionStore.GetAsync(sessionId, context.RequestAborted);
            if (session is not null)
            {
                await TryRevokeAsync(session.RefreshToken, context.RequestAborted);
            }

            await sessionStore.RemoveAsync(sessionId, CancellationToken.None);
        }

        await context.SignOutAsync(BffAuthConstants.CookieScheme);
        context.Response.StatusCode = StatusCodes.Status204NoContent;
    }

    private async Task ProxyAsync(HttpContext context, BackendSession session, string sessionId)
    {
        var client = httpClientFactory.CreateClient(HttpClientName);
        var targetPath = context.Request.Path.Value!;
        var queryString = context.Request.QueryString.Value ?? string.Empty;

        using var request = new HttpRequestMessage(
            new HttpMethod(context.Request.Method),
            $"{targetPath}{queryString}");

        request.Headers.Authorization =
            new AuthenticationHeaderValue("Bearer", session.AccessToken);

        CopyRequestHeaders(context.Request.Headers, request);

        if (HasBody(context.Request.Method) && context.Request.ContentLength > 0)
        {
            request.Content = new StreamContent(context.Request.Body);
            if (context.Request.ContentType is not null)
            {
                request.Content.Headers.TryAddWithoutValidation(
                    "Content-Type", context.Request.ContentType);
            }
        }

        var response = await client.SendAsync(request, context.RequestAborted);

        if (response.StatusCode == HttpStatusCode.Unauthorized)
        {
            logger.LogInformation(
                "Backend returned 401 for proxied {Method} {Path}; attempting transparent refresh.",
                context.Request.Method, targetPath);
            response.Dispose();

            try
            {
                var tokenResponse = await authApiClient.RefreshAsync(
                    session.RefreshToken, context.RequestAborted);

                if (string.IsNullOrWhiteSpace(tokenResponse.AccessToken)
                    || string.IsNullOrWhiteSpace(tokenResponse.RefreshToken)
                    || tokenResponse.ExpiresInSeconds <= 0)
                {
                    throw new BackendProtocolException(
                        "The backend refresh response is missing required token metadata.");
                }

                var refreshed = session.WithTokens(
                    tokenResponse.AccessToken,
                    tokenResponse.RefreshToken,
                    DateTimeOffset.UtcNow.AddSeconds(tokenResponse.ExpiresInSeconds));

                await sessionStore.SetAsync(sessionId, refreshed, context.RequestAborted);

                using var retry = new HttpRequestMessage(
                    new HttpMethod(context.Request.Method),
                    $"{targetPath}{queryString}");
                retry.Headers.Authorization =
                    new AuthenticationHeaderValue("Bearer", refreshed.AccessToken);
                CopyRequestHeaders(context.Request.Headers, retry);

                if (HasBody(context.Request.Method) && context.Request.ContentLength > 0)
                {
                    retry.Content = new StreamContent(context.Request.Body);
                    if (context.Request.ContentType is not null)
                    {
                        retry.Content.Headers.TryAddWithoutValidation(
                            "Content-Type", context.Request.ContentType);
                    }
                }

                response = await client.SendAsync(retry, context.RequestAborted);
            }
            catch (BackendApiException ex) when (ex.StatusCode == (int)HttpStatusCode.Unauthorized)
            {
                logger.LogInformation("Refresh token invalid/reused; ending session.");
                await sessionStore.RemoveAsync(sessionId, context.RequestAborted);
                await context.SignOutAsync(BffAuthConstants.CookieScheme);
                await WriteErrorAsync(context.Response, StatusCodes.Status401Unauthorized,
                    "SESSION_EXPIRED", "Sessiya bitib. Yeniden daxil olun.");
                return;
            }
        }

        await CopyResponseAsync(context, response);
    }

    private static void CopyRequestHeaders(IHeaderDictionary source, HttpRequestMessage target)
    {
        foreach (var header in source)
        {
            if (header.Key.Equals("Authorization", StringComparison.OrdinalIgnoreCase)
                || header.Key.Equals("Cookie", StringComparison.OrdinalIgnoreCase)
                || header.Key.Equals("Host", StringComparison.OrdinalIgnoreCase))
            {
                continue;
            }

            target.Headers.TryAddWithoutValidation(header.Key, header.Value.ToArray());
        }
    }

    private static bool HasBody(string method) =>
        HttpMethods.IsPost(method)
        || HttpMethods.IsPut(method)
        || HttpMethods.IsPatch(method);

    private static async Task CopyResponseAsync(HttpContext context, HttpResponseMessage response)
    {
        context.Response.StatusCode = (int)response.StatusCode;

        foreach (var header in response.Headers)
        {
            if (!header.Key.Equals("Transfer-Encoding", StringComparison.OrdinalIgnoreCase))
            {
                context.Response.Headers[header.Key] = header.Value.ToArray();
            }
        }

        if (response.Content is not null)
        {
            if (response.Content.Headers.ContentType is not null)
            {
                context.Response.ContentType = response.Content.Headers.ContentType.ToString();
            }

            var body = await response.Content.ReadAsByteArrayAsync();
            await context.Response.Body.WriteAsync(body);
        }
    }

    private async Task TryRevokeAsync(string refreshToken, CancellationToken ct)
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

    private static Task WriteErrorAsync(
        HttpResponse response, int statusCode, string code, string message)
    {
        response.StatusCode = statusCode;
        response.ContentType = "application/json";
        response.Headers.CacheControl = "no-store";
        return response.WriteAsJsonAsync(new ErrorResponse(code, message));
    }
}
