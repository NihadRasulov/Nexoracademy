using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ICourseReviewApiClient : IBackendCrudClient<CourseReviewResponse, CourseReviewRequest, long>;

public class CourseReviewApiClient(HttpClient httpClient)
    : BackendCrudClient<CourseReviewResponse, CourseReviewRequest, long>(httpClient, "/api/v1/course-reviews"), ICourseReviewApiClient;
