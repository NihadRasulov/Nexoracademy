using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Configuration;
using NexoraAcademy.AdminBff.Middleware;
using NexoraAcademy.AdminBff.Routing;

var builder = WebApplication.CreateBuilder(args);
var adminSettings = AdminSettings.Load(builder.Configuration);
builder.Services.AddSingleton(adminSettings);

builder.Services.AddControllers();
builder.Services.AddOpenApi();
builder.Services.AddHttpContextAccessor();
builder.Services.AddAdminSecurity(builder.Configuration, builder.Environment, adminSettings);
builder.Services.AddNexoraApiClients(builder.Configuration);

builder.Services.AddDataProtection();
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSingleton<ISessionStore, DistributedCacheSessionStore>();

var app = builder.Build();

app.UseForwardedHeaders();

if (!app.Environment.IsDevelopment())
{
    app.UseHsts();
}

app.UseMiddleware<SecurityHeadersMiddleware>();
app.UseMiddleware<ExceptionHandlingMiddleware>();
app.UseMiddleware<AdminRouteProtectionMiddleware>();

app.UseHttpsRedirection();
app.UseAdminStaticFiles(adminSettings);
app.UseRouting();
app.UseCors(AdminSecurityServiceCollectionExtensions.CorsPolicyName);
app.UseRateLimiter();
app.UseAuthentication();
app.UseAuthorization();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi($"{adminSettings.BasePath}/openapi/{{documentName}}.json");
}

app.MapGroup(adminSettings.BasePath)
    .MapControllers()
    .RequireAuthorization(BffAuthConstants.PanelAccessPolicy);
app.MapAdminSpa(adminSettings);

app.Run();
