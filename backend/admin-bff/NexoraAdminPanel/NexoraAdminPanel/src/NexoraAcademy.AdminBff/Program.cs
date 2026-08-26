using System.Net.Http.Headers;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Configuration;
using NexoraAcademy.AdminBff.Middleware;
using NexoraAcademy.AdminBff.Routing;

var builder = WebApplication.CreateBuilder(args);
var adminSettings = AdminSettings.Load(builder.Configuration);
builder.Services.AddSingleton(adminSettings);

builder.Services.AddHttpContextAccessor();
builder.Services.AddAdminSecurity(builder.Configuration, builder.Environment, adminSettings);
builder.Services.AddNexoraApiClients(builder.Configuration);

builder.Services.AddDataProtection();
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSingleton<ISessionStore, DistributedCacheSessionStore>();

var apiOptions = builder.Configuration
    .GetRequiredSection(NexoraApiOptions.SectionName)
    .Get<NexoraApiOptions>()
    ?? throw new InvalidOperationException("NexoraApi configuration is missing.");

builder.Services.AddHttpClient(ApiV1ProxyMiddleware.HttpClientName, client =>
{
    client.BaseAddress = new Uri(apiOptions.BaseUrl);
    client.Timeout = TimeSpan.FromSeconds(apiOptions.TimeoutSeconds);
    client.DefaultRequestHeaders.Accept.Add(
        new MediaTypeWithQualityHeaderValue("application/json"));
});

var app = builder.Build();

app.UseForwardedHeaders();

if (!app.Environment.IsDevelopment())
{
    app.UseHsts();
}

app.UseMiddleware<SecurityHeadersMiddleware>();
app.UseMiddleware<ExceptionHandlingMiddleware>();
app.UseMiddleware<AdminRouteProtectionMiddleware>();

app.UseAdminStaticFiles(adminSettings);
app.UseRouting();
app.UseCors(AdminSecurityServiceCollectionExtensions.CorsPolicyName);
app.UseRateLimiter();
app.UseAuthentication();
app.UseAuthorization();

app.UseMiddleware<ApiV1ProxyMiddleware>();

app.MapAdminSpa(adminSettings);

app.Run();
