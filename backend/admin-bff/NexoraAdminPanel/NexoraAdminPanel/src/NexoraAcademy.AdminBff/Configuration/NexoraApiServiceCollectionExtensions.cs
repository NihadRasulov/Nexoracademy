using System.Net.Http.Headers;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;

namespace NexoraAcademy.AdminBff.Configuration;

public static class NexoraApiServiceCollectionExtensions
{
    public static IServiceCollection AddNexoraApiClients(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        var section = configuration.GetRequiredSection(NexoraApiOptions.SectionName);
        var options = section.Get<NexoraApiOptions>()
            ?? throw new InvalidOperationException("NexoraApi configuration is missing.");

        if (!Uri.TryCreate(options.BaseUrl, UriKind.Absolute, out var baseUri)
            || (baseUri.Scheme != Uri.UriSchemeHttp && baseUri.Scheme != Uri.UriSchemeHttps))
        {
            throw new InvalidOperationException(
                $"{NexoraApiOptions.SectionName}:BaseUrl must be an absolute HTTP(S) URL.");
        }

        if (options.TimeoutSeconds is < 1 or > 120)
        {
            throw new InvalidOperationException(
                $"{NexoraApiOptions.SectionName}:TimeoutSeconds must be between 1 and 120.");
        }

        services.Configure<NexoraApiOptions>(section);
        services.AddTransient<BackendAuthorizationHandler>();

        services.AddHttpClient<IAuthApiClient, AuthApiClient>(ConfigureClient);
        AddAuthenticatedClient<IUserApiClient, UserApiClient>(services, ConfigureClient);

        return services;

        void ConfigureClient(HttpClient client)
        {
            client.BaseAddress = baseUri;
            client.Timeout = TimeSpan.FromSeconds(options.TimeoutSeconds);
            client.DefaultRequestHeaders.Accept.Add(
                new MediaTypeWithQualityHeaderValue("application/json"));
            client.DefaultRequestHeaders.UserAgent.ParseAdd("NexoraAcademy-AdminBff/1.0");
        }
    }

    private static void AddAuthenticatedClient<TInterface, TImplementation>(
        IServiceCollection services,
        Action<HttpClient> configureClient)
        where TInterface : class
        where TImplementation : class, TInterface
    {
        services.AddHttpClient<TInterface, TImplementation>(configureClient)
            .AddHttpMessageHandler<BackendAuthorizationHandler>();
    }
}
