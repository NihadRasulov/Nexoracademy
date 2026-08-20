using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/applications")]
[Authorize(Roles = Roles.AdminOnly)]
public class ApplicationController(IApplicationApiClient client)
    : BffCrudControllerBase<BackendC.ApplicationResponse, BackendC.ApplicationRequest, BffC.ApplicationResponse, BffC.ApplicationRequest, long>
{
    protected override IBackendCrudClient<BackendC.ApplicationResponse, BackendC.ApplicationRequest, long> Client => client;

    protected override BffC.ApplicationResponse ToBff(BackendC.ApplicationResponse r) =>
        new(r.Id, r.ApplicationType, r.Fullname, r.Email, r.Phone, r.Letter, r.CvFilename, r.Status, r.CreatedAt);

    protected override BackendC.ApplicationRequest ToBackend(BffC.ApplicationRequest r) =>
        new(r.ApplicationType, r.Fullname, r.Email, r.Phone, r.Letter);
}
