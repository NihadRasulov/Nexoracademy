using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/payments")]
[Authorize(Roles = Roles.AdminOnly)]
public class PaymentController(IPaymentApiClient client)
    : BffCrudControllerBase<BackendC.PaymentResponse, BackendC.PaymentRequest, BffC.PaymentResponse, BffC.PaymentRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.PaymentResponse, BackendC.PaymentRequest, Guid> Client => client;

    [HttpPost("{id:guid}/capture")]
    public async Task<ActionResult<BffC.PaymentResponse>> Capture(Guid id, CancellationToken ct)
    {
        var captured = await client.CaptureAsync(id, ct);
        return Ok(ToBff(captured));
    }

    protected override BffC.PaymentResponse ToBff(BackendC.PaymentResponse r) => new(
        r.Id, r.EnrollmentId, r.Method, r.Amount, r.Currency, r.Status, r.ExternalTxnId, r.IdempotencyKey,
        r.Installments, r.RefundAmount, r.RefundReason, r.InitiatedAt, r.CapturedAt, r.FailureReason);

    protected override BackendC.PaymentRequest ToBackend(BffC.PaymentRequest r) => new(
        r.EnrollmentId, r.Method, r.Amount, r.Currency, r.ExternalTxnId, r.IdempotencyKey, r.Installments);
}
