using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ICourseGroupApiClient : IBackendCrudClient<CourseGroupResponse, CourseGroupRequest, Guid>;

public class CourseGroupApiClient(HttpClient httpClient)
    : BackendCrudClient<CourseGroupResponse, CourseGroupRequest, Guid>(httpClient, "/api/v1/course-groups"), ICourseGroupApiClient;
