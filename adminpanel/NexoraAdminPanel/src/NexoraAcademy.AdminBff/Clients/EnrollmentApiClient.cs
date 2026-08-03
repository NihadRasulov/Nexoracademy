using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public class EnrollmentApiClient(HttpClient httpClient)
    : BackendCrudClient<EnrollmentResponse, EnrollmentRequest, Guid>(httpClient, "/api/v1/enrollments"), IEnrollmentApiClient
{
    public Task<EnrollmentResponse> CancelAsync(Guid id, CancelEnrollmentRequest request, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<EnrollmentResponse>(
            httpClient, HttpMethod.Post, $"/api/v1/enrollments/{id}/cancel", request, ct);
}
