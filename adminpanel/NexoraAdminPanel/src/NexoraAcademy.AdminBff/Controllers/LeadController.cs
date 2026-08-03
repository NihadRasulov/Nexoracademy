using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/sales/leads")]
[Authorize(Roles = Roles.SalesCrm)]
public class LeadController(ILeadApiClient client)
    : BffCrudControllerBase<BackendC.LeadResponse, BackendC.LeadRequest, BffC.LeadResponse, BffC.LeadRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.LeadResponse, BackendC.LeadRequest, Guid> Client => client;

    protected override BffC.LeadResponse ToBff(BackendC.LeadResponse r) => new(
        r.Id, r.FullName, r.Email, r.Phone, r.CourseId, r.Source, r.Status, r.AssignedTo, r.ConsentVersion,
        r.ConsentGivenAt, r.DuplicateOfLeadId, r.ActivityLog, r.CreatedAt, r.UpdatedAt);

    protected override BackendC.LeadRequest ToBackend(BffC.LeadRequest r) =>
        new(r.FullName, r.Email, r.Phone, r.CourseId, r.Source, r.AssignedTo, r.ConsentVersion);
}
