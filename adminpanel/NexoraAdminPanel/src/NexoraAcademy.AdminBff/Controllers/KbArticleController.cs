using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/kb-articles")]
[Authorize(Roles = Roles.ContentManager)]
public class KbArticleController(IKbArticleApiClient client)
    : BffCrudControllerBase<BackendC.KbArticleResponse, BackendC.KbArticleRequest, BffC.KbArticleResponse, BffC.KbArticleRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.KbArticleResponse, BackendC.KbArticleRequest, Guid> Client => client;

    protected override BffC.KbArticleResponse ToBff(BackendC.KbArticleResponse r) => new(
        r.Id, r.SourceType, r.SourceRefId, r.Title, r.Content, r.Active, r.UpdatedAt);

    protected override BackendC.KbArticleRequest ToBackend(BffC.KbArticleRequest r) => new(
        r.SourceType, r.SourceRefId, r.Title, r.Content, r.Active);
}
