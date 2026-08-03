using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/applications")]
[Authorize(Roles = Roles.AdminOnly)]
public class ApplicationController(IApplicationApiClient client)
    : BffCrudControllerBase<BackendC.ApplicationResponse, BackendC.ApplicationResponse, BffC.ApplicationResponse, BffC.ApplicationResponse, long>
{
    protected override IBackendCrudClient<BackendC.ApplicationResponse, BackendC.ApplicationResponse, long> Client => client;

    protected override BffC.ApplicationResponse ToBff(BackendC.ApplicationResponse r) => new(
        r.Id, r.ApplicationType, r.Fullname, r.Email, r.Phone, r.Letter, r.CvFilename, r.Status, r.CreatedAt);

    protected override BackendC.ApplicationResponse ToBackend(BffC.ApplicationResponse r) => new(
        r.Id, r.ApplicationType, r.Fullname, r.Email, r.Phone, r.Letter, r.CvFilename, r.Status, r.CreatedAt);
}
