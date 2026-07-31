using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IApplicationApiClient : IBackendCrudClient<ApplicationResponse, ApplicationResponse, long>;

public class ApplicationApiClient(HttpClient httpClient)
    : BackendCrudClient<ApplicationResponse, ApplicationResponse, long>(httpClient, "/api/v1/applications"),
        IApplicationApiClient;
