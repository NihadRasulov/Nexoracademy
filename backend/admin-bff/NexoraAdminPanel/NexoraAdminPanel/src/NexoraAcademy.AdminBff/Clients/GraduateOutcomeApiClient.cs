using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IGraduateOutcomeApiClient : IBackendCrudClient<GraduateOutcomeResponse, GraduateOutcomeRequest, long>;

public class GraduateOutcomeApiClient(HttpClient httpClient)
    : BackendCrudClient<GraduateOutcomeResponse, GraduateOutcomeRequest, long>(httpClient, "/api/v1/graduate-outcomes"), IGraduateOutcomeApiClient;
