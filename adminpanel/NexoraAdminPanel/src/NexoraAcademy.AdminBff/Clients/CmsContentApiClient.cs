using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ICmsContentApiClient : IBackendCrudClient<CmsContentResponse, CmsContentRequest, long>;

public class CmsContentApiClient(HttpClient httpClient)
    : BackendCrudClient<CmsContentResponse, CmsContentRequest, long>(httpClient, "/api/v1/content/cms-content"), ICmsContentApiClient;
