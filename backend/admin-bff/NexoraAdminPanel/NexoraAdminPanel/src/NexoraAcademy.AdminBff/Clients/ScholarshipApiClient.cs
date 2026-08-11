using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IScholarshipApiClient : IBackendCrudClient<ScholarshipResponse, ScholarshipRequest, short>;

public class ScholarshipApiClient(HttpClient httpClient)
    : BackendCrudClient<ScholarshipResponse, ScholarshipRequest, short>(httpClient, "/api/v1/scholarships"), IScholarshipApiClient;
