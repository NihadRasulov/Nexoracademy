using System.Text.Encodings.Web;
using Microsoft.AspNetCore.StaticFiles;
using NexoraAcademy.AdminBff.Configuration;

namespace NexoraAcademy.AdminBff.Routing;

public static class AdminSpaEndpointExtensions
{
    private static readonly FileExtensionContentTypeProvider ContentTypeProvider = new();

    public static void UseAdminStaticFiles(this WebApplication app, AdminSettings settings)
    {
        app.UseStaticFiles(new StaticFileOptions
        {
            RequestPath = settings.BasePath,
            OnPrepareResponse = context =>
            {
                if (string.Equals(
                        context.File.Name,
                        "index.html",
                        StringComparison.OrdinalIgnoreCase))
                {
                    context.Context.Response.Headers.CacheControl = "no-store";
                    return;
                }

                context.Context.Response.Headers.CacheControl =
                    context.Context.Request.Path.StartsWithSegments(
                        settings.BasePath.Add("/assets"),
                        StringComparison.OrdinalIgnoreCase)
                        ? "public,max-age=31536000,immutable"
                        : "public,max-age=86400";
            }
        });
    }

    public static void MapAdminSpa(this WebApplication app, AdminSettings settings)
    {
        var pattern = $"{settings.BasePath}/{{**spaPath}}";
        app.MapGet(pattern, async context =>
        {
            if (IsApiOrFileRequest(context.Request.Path, settings.BasePath))
            {
                context.Response.StatusCode = StatusCodes.Status404NotFound;
                return;
            }

            var environment = context.RequestServices.GetRequiredService<IWebHostEnvironment>();
            var indexFile = environment.WebRootFileProvider.GetFileInfo("index.html");
            if (!indexFile.Exists)
            {
                context.Response.StatusCode = StatusCodes.Status404NotFound;
                return;
            }

            using var reader = new StreamReader(indexFile.CreateReadStream());
            var html = await reader.ReadToEndAsync(context.RequestAborted);
            var baseHref = HtmlEncoder.Default.Encode($"{settings.BasePath}/");
            html = html.Replace(
                "<head>",
                $"<head>{Environment.NewLine}    <base href=\"{baseHref}\" />",
                StringComparison.OrdinalIgnoreCase);

            context.Response.ContentType = "text/html; charset=utf-8";
            context.Response.Headers.CacheControl = "no-store";
            await context.Response.WriteAsync(html, context.RequestAborted);
        });
    }

    private static bool IsApiOrFileRequest(PathString requestPath, PathString basePath)
    {
        var apiPath = basePath.Add("/api");
        if (requestPath.StartsWithSegments(apiPath, StringComparison.OrdinalIgnoreCase))
        {
            return true;
        }

        var relativePath = requestPath.Value?[basePath.Value!.Length..] ?? string.Empty;
        return ContentTypeProvider.TryGetContentType(relativePath, out _);
    }
}
