using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/cms-content")]
[Authorize(Roles = Roles.ContentManager)]
public class CmsContentController(ICmsContentApiClient client)
    : BffCrudControllerBase<BackendC.CmsContentResponse, BackendC.CmsContentRequest, BffC.CmsContentResponse, BffC.CmsContentRequest, long>
{
    protected override IBackendCrudClient<BackendC.CmsContentResponse, BackendC.CmsContentRequest, long> Client => client;

    protected override BffC.CmsContentResponse ToBff(BackendC.CmsContentResponse r) =>
        new(r.Id, r.Key, r.Type, r.Title, r.Body, r.Data, r.Published, r.SortOrder, r.UpdatedBy, r.UpdatedAt);

    protected override BackendC.CmsContentRequest ToBackend(BffC.CmsContentRequest r) =>
        new(r.Key, r.Type, r.Title, r.Body, r.Data, r.Published, r.SortOrder);
}
