using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IOAuthAccountApiClient : IBackendCrudClient<OAuthAccountResponse, OAuthAccountRequest, long>;

public class OAuthAccountApiClient(HttpClient httpClient)
    : BackendCrudClient<OAuthAccountResponse, OAuthAccountRequest, long>(httpClient, "/api/v1/oauth-accounts"), IOAuthAccountApiClient;
