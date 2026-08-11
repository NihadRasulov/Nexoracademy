using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/enrollments")]
[Authorize(Roles = Roles.SalesCrm)]
public class EnrollmentController(IEnrollmentApiClient client)
    : BffCrudControllerBase<BackendC.EnrollmentResponse, BackendC.EnrollmentRequest, BffC.EnrollmentResponse, BffC.EnrollmentRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.EnrollmentResponse, BackendC.EnrollmentRequest, Guid> Client => client;

    [HttpPost("{id:guid}/cancel")]
    public async Task<ActionResult<BffC.EnrollmentResponse>> Cancel(
        Guid id, [FromBody] BffC.CancelEnrollmentRequest? request, CancellationToken ct)
    {
        var cancelled = await client.CancelAsync(id, new BackendC.CancelEnrollmentRequest(request?.Reason), ct);
        return Ok(ToBff(cancelled));
    }

    protected override BffC.EnrollmentResponse ToBff(BackendC.EnrollmentResponse r) => new(
        r.Id, r.UserId, r.GroupId, r.Status.ToString(), r.IdempotencyKey, r.ConsentVersion,
        r.ConsentGivenAt, r.HoldExpiresAt, r.EnrolledAt, r.CompletedAt, r.CancelledAt, r.CancelReason);

    protected override BackendC.EnrollmentRequest ToBackend(BffC.EnrollmentRequest r) => new(
        r.UserId, r.GroupId,
        r.Status is null ? null : Enum.Parse<BackendC.EnrollmentStatus>(r.Status),
        r.IdempotencyKey, r.ConsentVersion, r.ConsentGivenAt);
}
