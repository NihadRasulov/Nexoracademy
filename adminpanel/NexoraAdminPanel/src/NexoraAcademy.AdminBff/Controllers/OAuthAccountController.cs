using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/oauth-accounts")]
[Authorize(Roles = Roles.AdminOnly)]
public class OAuthAccountController(IOAuthAccountApiClient client)
    : BffCrudControllerBase<BackendC.OAuthAccountResponse, BackendC.OAuthAccountRequest, BffC.OAuthAccountResponse, BffC.OAuthAccountRequest, long>
{
    protected override IBackendCrudClient<BackendC.OAuthAccountResponse, BackendC.OAuthAccountRequest, long> Client => client;

    protected override BffC.OAuthAccountResponse ToBff(BackendC.OAuthAccountResponse r) => new(
        r.Id, r.UserId, r.Provider, r.ProviderUserId, r.LinkedAt);

    protected override BackendC.OAuthAccountRequest ToBackend(BffC.OAuthAccountRequest r) => new(
        r.UserId, r.Provider, r.ProviderUserId, r.AccessTokenEnc, r.RefreshTokenEnc);
}
