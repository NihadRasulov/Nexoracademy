using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IKbArticleApiClient : IBackendCrudClient<KbArticleResponse, KbArticleRequest, Guid>;

public class KbArticleApiClient(HttpClient httpClient)
    : BackendCrudClient<KbArticleResponse, KbArticleRequest, Guid>(httpClient, "/api/v1/kb-articles"), IKbArticleApiClient;
