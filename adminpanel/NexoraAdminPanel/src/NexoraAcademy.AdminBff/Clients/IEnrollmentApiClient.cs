using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IEnrollmentApiClient : IBackendCrudClient<EnrollmentResponse, EnrollmentRequest, Guid>
{
    Task<EnrollmentResponse> CancelAsync(Guid id, CancelEnrollmentRequest request, CancellationToken ct = default);
}
