using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/categories")]
[Authorize(Roles = Roles.ContentManager)]
public class CategoryController(ICategoryApiClient client)
    : BffCrudControllerBase<BackendC.CategoryResponse, BackendC.CategoryRequest, BffC.CategoryResponse, BffC.CategoryRequest, short>
{
    protected override IBackendCrudClient<BackendC.CategoryResponse, BackendC.CategoryRequest, short> Client => client;

    protected override BffC.CategoryResponse ToBff(BackendC.CategoryResponse r) =>
        new(r.Id, r.Slug, r.Name, r.ParentId, r.SortOrder, r.Active);

    protected override BackendC.CategoryRequest ToBackend(BffC.CategoryRequest r) =>
        new(r.Slug, r.Name, r.ParentId, r.SortOrder, r.Active);
}
