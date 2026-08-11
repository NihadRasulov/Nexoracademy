using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ISessionApiClient : IBackendCrudClient<SessionResponse, SessionRequest, Guid>;

public class SessionApiClient(HttpClient httpClient)
    : BackendCrudClient<SessionResponse, SessionRequest, Guid>(httpClient, "/api/v1/sessions"), ISessionApiClient;
