using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ILeadApiClient : IBackendCrudClient<LeadResponse, LeadRequest, Guid>;

public class LeadApiClient(HttpClient httpClient)
    : BackendCrudClient<LeadResponse, LeadRequest, Guid>(httpClient, "/api/v1/sales/leads"), ILeadApiClient;
