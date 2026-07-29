using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IPaymentApiClient : IBackendCrudClient<PaymentResponse, PaymentRequest, Guid>
{
    Task<PaymentResponse> CaptureAsync(Guid id, CancellationToken ct = default);
}
