using Microsoft.AspNetCore.Authentication.Cookies;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Contracts.Bff;
using NexoraAcademy.AdminBff.Middleware;
using Microsoft.AspNetCore.HttpOverrides;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddOpenApi();
builder.Services.AddHttpContextAccessor();

builder.Services.AddDistributedMemoryCache();
builder.Services.AddSingleton<ISessionStore, DistributedCacheSessionStore>();

var nexoraApiSection = builder.Configuration.GetSection(NexoraApiOptions.SectionName);
builder.Services.Configure<NexoraApiOptions>(nexoraApiSection);
var nexoraApiBaseUrl = nexoraApiSection["BaseUrl"]
    ?? throw new InvalidOperationException($"{NexoraApiOptions.SectionName}:BaseUrl konfiqurasiya edilmeyib.");

builder.Services.Configure<ForwardedHeadersOptions>(options =>
{
    options.ForwardedHeaders = ForwardedHeaders.XForwardedFor | ForwardedHeaders.XForwardedProto;
    options.KnownNetworks.Clear();
    options.KnownProxies.Clear();
});

builder.Services.AddHttpClient<IAuthApiClient, AuthApiClient>(client =>
{
    client.BaseAddress = new Uri(nexoraApiBaseUrl);
});

builder.Services.AddHttpClient<IHealthApiClient, HealthApiClient>(client =>
{
    client.BaseAddress = new Uri(nexoraApiBaseUrl);
});

builder.Services.AddTransient<BackendAuthorizationHandler>();

void AddAuthenticatedClient<TInterface, TImplementation>()
    where TInterface : class
    where TImplementation : class, TInterface
{
    builder.Services.AddHttpClient<TInterface, TImplementation>(client =>
    {
        client.BaseAddress = new Uri(nexoraApiBaseUrl);
    }).AddHttpMessageHandler<BackendAuthorizationHandler>();
}

AddAuthenticatedClient<IUserApiClient, UserApiClient>();
AddAuthenticatedClient<ICategoryApiClient, CategoryApiClient>();
AddAuthenticatedClient<ICourseApiClient, CourseApiClient>();
AddAuthenticatedClient<ICourseInstructorApiClient, CourseInstructorApiClient>();
AddAuthenticatedClient<IInstructorApiClient, InstructorApiClient>();
AddAuthenticatedClient<ICourseGroupApiClient, CourseGroupApiClient>();
AddAuthenticatedClient<IEnrollmentApiClient, EnrollmentApiClient>();
AddAuthenticatedClient<IPaymentApiClient, PaymentApiClient>();
AddAuthenticatedClient<IScholarshipApiClient, ScholarshipApiClient>();
AddAuthenticatedClient<ICmsContentApiClient, CmsContentApiClient>();
AddAuthenticatedClient<ICampaignApiClient, CampaignApiClient>();
AddAuthenticatedClient<IChatSessionApiClient, ChatSessionApiClient>();
AddAuthenticatedClient<IContactSubmissionApiClient, ContactSubmissionApiClient>();
AddAuthenticatedClient<ILeadApiClient, LeadApiClient>();
AddAuthenticatedClient<IOAuthAccountApiClient, OAuthAccountApiClient>();
AddAuthenticatedClient<ISessionApiClient, SessionApiClient>();
AddAuthenticatedClient<INotificationApiClient, NotificationApiClient>();
AddAuthenticatedClient<IKbArticleApiClient, KbArticleApiClient>();
AddAuthenticatedClient<ICourseReviewApiClient, CourseReviewApiClient>();
AddAuthenticatedClient<IGraduateOutcomeApiClient, GraduateOutcomeApiClient>();
AddAuthenticatedClient<IAuditLogApiClient, AuditLogApiClient>();
AddAuthenticatedClient<IApplicationApiClient, ApplicationApiClient>();

// Defaults to "secure cookies required" outside Development (previous behavior).
// Set Cookie__RequireHttps=false explicitly (env var) for a plain-HTTP/IP-only
// deployment that has no TLS terminator yet — remove the override once a
// domain + HTTPS (e.g. via nginx + Let's Encrypt) is in front of the BFF.
var requireHttpsCookie = builder.Configuration.GetValue<bool?>("Cookie:RequireHttps")
    ?? !builder.Environment.IsDevelopment();

builder.Services.AddAuthentication(BffAuthConstants.CookieScheme)
    .AddCookie(BffAuthConstants.CookieScheme, options =>
    {
        options.Cookie.Name = BffAuthConstants.CookieName;
        options.Cookie.HttpOnly = true;
        options.Cookie.SameSite = SameSiteMode.Lax;
        options.Cookie.SecurePolicy = requireHttpsCookie
            ? CookieSecurePolicy.Always
            : CookieSecurePolicy.SameAsRequest;
        options.ExpireTimeSpan = TimeSpan.FromDays(30);
        options.SlidingExpiration = true;
        options.Events.OnRedirectToLogin = context =>
        {
            context.Response.StatusCode = StatusCodes.Status401Unauthorized;
            context.Response.ContentType = "application/json";
            return context.Response.WriteAsJsonAsync(
                new ErrorResponse("UNAUTHORIZED", "Daxil olmaq teleb olunur."));
        };
        options.Events.OnRedirectToAccessDenied = context =>
        {
            context.Response.StatusCode = StatusCodes.Status403Forbidden;
            context.Response.ContentType = "application/json";
            return context.Response.WriteAsJsonAsync(
                new ErrorResponse("FORBIDDEN", "Bu emeliyyat ucun icazeniz yoxdur."));
        };
    });
builder.Services.AddAuthorization();

var corsOrigins = builder.Configuration.GetSection("Cors:AllowedOrigins").Get<string[]>() ?? [];
const string corsPolicy = "AdminPanelFrontend";
builder.Services.AddCors(options =>
{
    options.AddPolicy(corsPolicy, policy =>
    {
        policy.WithOrigins(corsOrigins)
            .AllowAnyMethod()
            .AllowAnyHeader()
            .AllowCredentials();
    });
});

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}
app.UseForwardedHeaders();
app.UseMiddleware<ExceptionHandlingMiddleware>();

// if (requireHttpsCookie)
// {
//    app.UseHttpsRedirection();
//}
app.UseCors(corsPolicy);
app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

app.Run();
