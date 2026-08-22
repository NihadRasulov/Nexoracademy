using System.Globalization;
using System.Net;
using System.Threading.RateLimiting;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.HttpOverrides;
using Microsoft.AspNetCore.RateLimiting;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Configuration;

public static class AdminSecurityServiceCollectionExtensions
{
    public const string CorsPolicyName = "AdminPanelFrontend";

    public static IServiceCollection AddAdminSecurity(
        this IServiceCollection services,
        IConfiguration configuration,
        IWebHostEnvironment environment,
        AdminSettings settings)
    {
        services.ConfigureAuthentication(environment, settings);
        services.ConfigureAuthorization();
        services.ConfigureCors(configuration);
        services.ConfigureTrustedProxies(configuration);
        services.ConfigureLoginRateLimit(settings);
        return services;
    }

    private static void ConfigureAuthentication(
        this IServiceCollection services,
        IWebHostEnvironment environment,
        AdminSettings settings)
    {
        services.AddAuthentication(BffAuthConstants.CookieScheme)
            .AddCookie(BffAuthConstants.CookieScheme, options =>
            {
                options.Cookie.Name = BffAuthConstants.CookieName;
                options.Cookie.HttpOnly = true;
                options.Cookie.IsEssential = true;
                options.Cookie.Path = "/";
                options.Cookie.SameSite = SameSiteMode.Lax;
                options.Cookie.SecurePolicy = environment.IsDevelopment()
                    ? CookieSecurePolicy.SameAsRequest
                    : CookieSecurePolicy.Always;
                options.ExpireTimeSpan = TimeSpan.FromMinutes(settings.SessionIdleTimeoutMinutes);
                options.SlidingExpiration = true;

                options.Events.OnRedirectToLogin = context => WriteAuthErrorAsync(
                    context.Response,
                    StatusCodes.Status401Unauthorized,
                    new ErrorResponse("UNAUTHORIZED", "Daxil olmaq teleb olunur."));
                options.Events.OnRedirectToAccessDenied = context => WriteAuthErrorAsync(
                    context.Response,
                    StatusCodes.Status403Forbidden,
                    new ErrorResponse("FORBIDDEN", "Bu emeliyyat ucun icazeniz yoxdur."));
            });
    }

    private static void ConfigureAuthorization(this IServiceCollection services)
    {
        services.AddAuthorization(options =>
        {
            options.AddPolicy(BffAuthConstants.PanelAccessPolicy, policy =>
            {
                policy.RequireAuthenticatedUser();
                policy.RequireRole(Roles.PanelAccess.Split(','));
            });
        });
    }

    private static void ConfigureCors(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        var allowedOrigins = configuration
            .GetSection("Cors:AllowedOrigins")
            .Get<string[]>() ?? [];

        services.AddCors(options =>
        {
            options.AddPolicy(CorsPolicyName, policy =>
            {
                policy.WithOrigins(allowedOrigins)
                    .AllowAnyMethod()
                    .AllowAnyHeader()
                    .AllowCredentials();
            });
        });
    }

    private static void ConfigureTrustedProxies(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        var knownProxies = configuration
            .GetSection("ReverseProxy:KnownProxies")
            .Get<string[]>()?
            .Select(value => IPAddress.TryParse(value, out var address)
                ? address
                : throw new InvalidOperationException(
                    $"ReverseProxy:KnownProxies daxilinde yanlis IP unvani: {value}"))
            .ToArray() ?? [];

        services.Configure<ForwardedHeadersOptions>(options =>
        {
            options.ForwardedHeaders =
                ForwardedHeaders.XForwardedFor | ForwardedHeaders.XForwardedProto;
            options.ForwardLimit = 1;

            foreach (var proxy in knownProxies)
            {
                options.KnownProxies.Add(proxy);
            }
        });
    }

    private static void ConfigureLoginRateLimit(
        this IServiceCollection services,
        AdminSettings settings)
    {
        services.AddRateLimiter(options =>
        {
            options.RejectionStatusCode = StatusCodes.Status429TooManyRequests;
            options.OnRejected = async (context, cancellationToken) =>
            {
                if (context.Lease.TryGetMetadata(MetadataName.RetryAfter, out var retryAfter))
                {
                    context.HttpContext.Response.Headers.RetryAfter = Math
                        .Ceiling(retryAfter.TotalSeconds)
                        .ToString(CultureInfo.InvariantCulture);
                }

                var logger = context.HttpContext.RequestServices
                    .GetRequiredService<ILoggerFactory>()
                    .CreateLogger("AdminLoginRateLimit");
                logger.LogWarning(
                    "Admin login rate limit exceeded for client {RemoteIpAddress}.",
                    GetClientIp(context.HttpContext));

                context.HttpContext.Response.ContentType = "application/json";
                context.HttpContext.Response.Headers.CacheControl = "no-store";
                await context.HttpContext.Response.WriteAsJsonAsync(
                    new ErrorResponse(
                        "RATE_LIMITED",
                        "Cox sayda giris cehdi edildi. Bir qeder sonra yeniden yoxlayin."),
                    cancellationToken);
            };

            options.AddPolicy(BffRateLimitPolicies.Login, context =>
                RateLimitPartition.GetFixedWindowLimiter(
                    partitionKey: GetClientIp(context),
                    factory: _ => new FixedWindowRateLimiterOptions
                    {
                        AutoReplenishment = true,
                        PermitLimit = settings.LoginRateLimit.PermitLimit,
                        Window = TimeSpan.FromSeconds(settings.LoginRateLimit.WindowSeconds),
                        QueueLimit = 0,
                        QueueProcessingOrder = QueueProcessingOrder.OldestFirst
                    }));
        });
    }

    private static string GetClientIp(HttpContext context)
    {
        var address = context.Connection.RemoteIpAddress;
        if (address?.IsIPv4MappedToIPv6 == true)
        {
            address = address.MapToIPv4();
        }

        return address?.ToString() ?? "unknown";
    }

    private static Task WriteAuthErrorAsync(
        HttpResponse response,
        int statusCode,
        ErrorResponse error)
    {
        response.StatusCode = statusCode;
        response.ContentType = "application/json";
        response.Headers.CacheControl = "no-store";
        return response.WriteAsJsonAsync(error);
    }
}
