using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/scholarships")]
[Authorize(Roles = Roles.AdminOnly)]
public class ScholarshipController(IScholarshipApiClient client)
    : BffCrudControllerBase<BackendC.ScholarshipResponse, BackendC.ScholarshipRequest, BffC.ScholarshipResponse, BffC.ScholarshipRequest, short>
{
    protected override IBackendCrudClient<BackendC.ScholarshipResponse, BackendC.ScholarshipRequest, short> Client => client;

    protected override BffC.ScholarshipResponse ToBff(BackendC.ScholarshipResponse r) =>
        new(r.Id, r.Name, r.Description, r.DiscountPct, r.MaxRecipients, r.ValidFrom, r.ValidUntil, r.Active, r.Applications);

    protected override BackendC.ScholarshipRequest ToBackend(BffC.ScholarshipRequest r) =>
        new(r.Name, r.Description, r.DiscountPct, r.MaxRecipients, r.ValidFrom, r.ValidUntil, r.Active);
}
