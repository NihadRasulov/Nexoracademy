using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IInstructorApiClient : IBackendCrudClient<InstructorResponse, InstructorRequest, Guid>;

public class InstructorApiClient(HttpClient httpClient)
    : BackendCrudClient<InstructorResponse, InstructorRequest, Guid>(httpClient, "/api/v1/instructors"), IInstructorApiClient;
