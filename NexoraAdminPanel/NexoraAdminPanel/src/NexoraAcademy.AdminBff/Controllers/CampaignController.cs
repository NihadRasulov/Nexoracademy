using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/sales/campaigns")]
[Authorize(Roles = Roles.SalesCrm)]
public class CampaignController(ICampaignApiClient client)
    : BffCrudControllerBase<BackendC.CampaignResponse, BackendC.CampaignRequest, BffC.CampaignResponse, BffC.CampaignRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.CampaignResponse, BackendC.CampaignRequest, Guid> Client => client;

    protected override BffC.CampaignResponse ToBff(BackendC.CampaignResponse r) => new(
        r.Id, r.Name, r.BannerImageUrl, r.CtaUrl, r.DiscountPct, r.StartsAt, r.EndsAt, r.Active, r.Priority, r.CourseIds);

    protected override BackendC.CampaignRequest ToBackend(BffC.CampaignRequest r) => new(
        r.Name, r.BannerImageUrl, r.CtaUrl, r.DiscountPct, r.StartsAt, r.EndsAt, r.Active, r.Priority, r.CourseIds);
}
