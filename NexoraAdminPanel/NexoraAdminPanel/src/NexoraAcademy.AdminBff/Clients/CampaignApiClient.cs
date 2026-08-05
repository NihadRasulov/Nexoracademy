using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ICampaignApiClient : IBackendCrudClient<CampaignResponse, CampaignRequest, Guid>;

public class CampaignApiClient(HttpClient httpClient)
    : BackendCrudClient<CampaignResponse, CampaignRequest, Guid>(httpClient, "/api/v1/sales/campaigns"), ICampaignApiClient;
