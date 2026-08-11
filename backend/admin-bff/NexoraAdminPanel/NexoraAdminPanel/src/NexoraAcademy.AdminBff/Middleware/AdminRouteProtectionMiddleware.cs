using NexoraAcademy.AdminBff.Configuration;

namespace NexoraAcademy.AdminBff.Middleware;

public sealed class AdminRouteProtectionMiddleware(RequestDelegate next, AdminSettings settings)
{
    private static readonly PathString[] PublicAdminPaths =
    [
        new("/admin"),
        new("/administrator")
    ];

    public Task InvokeAsync(HttpContext context)
    {
        if (MustLookMissing(context.Request.Path, settings.BasePath))
        {
            context.Response.StatusCode = StatusCodes.Status404NotFound;
            return Task.CompletedTask;
        }

        return next(context);
    }

    private static bool MustLookMissing(PathString requestPath, PathString secretBasePath)
    {
        if (PublicAdminPaths.Any(path =>
                requestPath.StartsWithSegments(path, StringComparison.OrdinalIgnoreCase)))
        {
            return true;
        }

        var exposedIndexPath = secretBasePath.Add("/index.html");
        return requestPath.Equals(exposedIndexPath, StringComparison.OrdinalIgnoreCase);
    }
}
