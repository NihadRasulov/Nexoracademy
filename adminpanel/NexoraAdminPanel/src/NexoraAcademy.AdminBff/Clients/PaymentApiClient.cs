using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public class PaymentApiClient(HttpClient httpClient)
    : BackendCrudClient<PaymentResponse, PaymentRequest, Guid>(httpClient, "/api/v1/payments"), IPaymentApiClient
{
    public Task<PaymentResponse> CaptureAsync(Guid id, CancellationToken ct = default) =>
        BackendHttpJson.SendAsync<PaymentResponse>(
            httpClient, HttpMethod.Post, $"/api/v1/payments/{id}/capture", null, ct);
}
