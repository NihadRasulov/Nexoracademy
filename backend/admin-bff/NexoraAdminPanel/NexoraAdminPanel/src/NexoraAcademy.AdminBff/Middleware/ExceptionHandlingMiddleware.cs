using System.Net;
using Microsoft.AspNetCore.Authentication;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Middleware;

public class ExceptionHandlingMiddleware(RequestDelegate next, ILogger<ExceptionHandlingMiddleware> logger)
{
    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await next(context);
        }
        catch (SessionExpiredException)
        {
            await context.SignOutAsync(BffAuthConstants.CookieScheme);
            await WriteAsync(context, HttpStatusCode.Unauthorized,
                new ErrorResponse("SESSION_EXPIRED", "Sessiyanin muddeti bitib, yeniden daxil olun."));
        }
        catch (OtpRequiredException)
        {
            await WriteAsync(context, HttpStatusCode.Unauthorized,
                new ErrorResponse("OTP_REQUIRED", "Bu hesab ucun OTP tesdiqi teleb olunur, admin panelinden istifade edile bilmez."));
        }
        catch (PanelAccessDeniedException)
        {
            await WriteAsync(context, HttpStatusCode.Forbidden,
                new ErrorResponse("PANEL_ACCESS_DENIED", "Bu hesab admin paneline daxil ola bilmez."));
        }
        catch (BackendApiException ex)
        {
            LogBackendError(ex);
            await WriteAsync(context, MapStatus(ex.StatusCode), MapError(ex));
        }
        catch (BackendProtocolException ex)
        {
            logger.LogError(ex, "Nexora backend returned an invalid response.");
            await WriteAsync(context, HttpStatusCode.BadGateway,
                new ErrorResponse("BACKEND_PROTOCOL_ERROR", "Backend servisi yanlis formatda cavab qaytardi."));
        }
        catch (HttpRequestException ex)
        {
            logger.LogError(ex, "Nexora backend-e qosulma alinmadi.");
            await WriteAsync(context, HttpStatusCode.BadGateway,
                new ErrorResponse("BACKEND_UNREACHABLE", "Backend servisine qosulmaq mumkun olmadi."));
        }
        catch (TaskCanceledException ex) when (!context.RequestAborted.IsCancellationRequested)
        {
            logger.LogError(ex, "Nexora backend-e sorgu vaxti bitdi (timeout).");
            await WriteAsync(context, HttpStatusCode.GatewayTimeout,
                new ErrorResponse("BACKEND_TIMEOUT", "Backend servisi vaxtinda cavab vermedi."));
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Gozlenilmez xeta.");
            await WriteAsync(context, HttpStatusCode.InternalServerError,
                new ErrorResponse("INTERNAL_ERROR", "Gozlenilmez xeta bas verdi."));
        }
    }

    private static HttpStatusCode MapStatus(int backendStatus) =>
        backendStatus >= 500 ? HttpStatusCode.BadGateway : (HttpStatusCode)backendStatus;

    private static ErrorResponse MapError(BackendApiException ex)
    {
        if (ex.Backend is null)
        {
            return new ErrorResponse("BACKEND_ERROR", ex.Message);
        }

        var code = ex.StatusCode switch
        {
            400 => "VALIDATION_ERROR",
            401 => "UNAUTHORIZED",
            403 => "FORBIDDEN",
            404 => "NOT_FOUND",
            409 => "CONFLICT",
            _ => "BACKEND_ERROR"
        };

        return new ErrorResponse(code, ex.Backend.Message, ex.Backend.Errors);
    }

    private void LogBackendError(BackendApiException ex) =>
        logger.LogWarning(
            "Backend {Status} on {Path}: {Message}",
            ex.StatusCode, ex.Backend?.Path, ex.Backend?.Message ?? ex.Message);

    private static Task WriteAsync(HttpContext context, HttpStatusCode status, ErrorResponse error)
    {
        context.Response.StatusCode = (int)status;
        context.Response.ContentType = "application/json";
        return context.Response.WriteAsJsonAsync(error);
    }
}
