using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/sessions")]
[Authorize(Roles = Roles.AdminOnly)]
public class SessionController(ISessionApiClient client)
    : BffCrudControllerBase<BackendC.SessionResponse, BackendC.SessionRequest, BffC.SessionResponse, BffC.SessionRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.SessionResponse, BackendC.SessionRequest, Guid> Client => client;

    protected override BffC.SessionResponse ToBff(BackendC.SessionResponse r) => new(
        r.Id, r.UserId, r.Type, r.IpAddress, r.UserAgent, r.IssuedAt, r.ExpiresAt, r.UsedAt, r.RevokedAt);

    protected override BackendC.SessionRequest ToBackend(BffC.SessionRequest r) => new(
        r.UserId, r.Type, r.TokenHash, r.IpAddress, r.UserAgent, r.ExpiresAt);
}
