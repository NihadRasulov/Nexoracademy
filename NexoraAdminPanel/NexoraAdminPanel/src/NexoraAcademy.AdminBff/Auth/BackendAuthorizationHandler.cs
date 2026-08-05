using System.Net;
using System.Net.Http.Headers;
using NexoraAcademy.AdminBff.Clients;

namespace NexoraAcademy.AdminBff.Auth;

public class BackendAuthorizationHandler(
    IHttpContextAccessor httpContextAccessor,
    ISessionStore sessionStore,
    IAuthApiClient authApiClient,
    ILogger<BackendAuthorizationHandler> logger) : DelegatingHandler
{
    protected override async Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request, CancellationToken cancellationToken)
    {
        var httpContext = httpContextAccessor.HttpContext
            ?? throw new InvalidOperationException("BackendAuthorizationHandler requires an active HttpContext.");

        var sessionId = httpContext.User.FindFirst(BffAuthConstants.SessionIdClaimType)?.Value
            ?? throw new SessionExpiredException();

        var session = await sessionStore.GetAsync(sessionId, cancellationToken)
            ?? throw new SessionExpiredException();

        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", session.AccessToken);
        var response = await base.SendAsync(request, cancellationToken);

        if (response.StatusCode != HttpStatusCode.Unauthorized)
        {
            return response;
        }

        logger.LogInformation("Backend returned 401 for an admin session; attempting transparent refresh.");
        response.Dispose();

        BackendSession refreshed;
        try
        {
            var tokenResponse = await authApiClient.RefreshAsync(session.RefreshToken, cancellationToken);
            if (string.IsNullOrWhiteSpace(tokenResponse.AccessToken)
                || string.IsNullOrWhiteSpace(tokenResponse.RefreshToken)
                || tokenResponse.ExpiresInSeconds <= 0)
            {
                throw new BackendProtocolException(
                    "The backend refresh response is missing required token metadata.");
            }

            refreshed = session.WithTokens(
                tokenResponse.AccessToken,
                tokenResponse.RefreshToken,
                DateTimeOffset.UtcNow.AddSeconds(tokenResponse.ExpiresInSeconds));
        }
        catch (BackendApiException ex) when (ex.StatusCode == (int)HttpStatusCode.Unauthorized)
        {
            logger.LogInformation("Refresh token invalid/reused for an admin session; ending session.");
            await sessionStore.RemoveAsync(sessionId, cancellationToken);
            throw new SessionExpiredException();
        }

        await sessionStore.SetAsync(sessionId, refreshed, cancellationToken);

        using var retryRequest = await CloneAsync(request, cancellationToken);
        retryRequest.Headers.Authorization = new AuthenticationHeaderValue("Bearer", refreshed.AccessToken);
        return await base.SendAsync(retryRequest, cancellationToken);
    }

    private static async Task<HttpRequestMessage> CloneAsync(HttpRequestMessage original, CancellationToken ct)
    {
        var clone = new HttpRequestMessage(original.Method, original.RequestUri)
        {
            Version = original.Version
        };

        if (original.Content is not null)
        {
            var bytes = await original.Content.ReadAsByteArrayAsync(ct);
            clone.Content = new ByteArrayContent(bytes);
            foreach (var header in original.Content.Headers)
            {
                clone.Content.Headers.Add(header.Key, header.Value);
            }
        }

        foreach (var header in original.Headers)
        {
            clone.Headers.TryAddWithoutValidation(header.Key, header.Value);
        }

        return clone;
    }
}
