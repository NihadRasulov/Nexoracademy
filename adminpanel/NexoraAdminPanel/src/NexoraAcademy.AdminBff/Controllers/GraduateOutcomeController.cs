using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/graduate-outcomes")]
[Authorize(Roles = Roles.ContentManager)]
public class GraduateOutcomeController(IGraduateOutcomeApiClient client)
    : BffCrudControllerBase<BackendC.GraduateOutcomeResponse, BackendC.GraduateOutcomeRequest, BffC.GraduateOutcomeResponse, BffC.GraduateOutcomeRequest, long>
{
    protected override IBackendCrudClient<BackendC.GraduateOutcomeResponse, BackendC.GraduateOutcomeRequest, long> Client => client;

    protected override BffC.GraduateOutcomeResponse ToBff(BackendC.GraduateOutcomeResponse r) => new(
        r.Id, r.UserId, r.CourseId, r.CompanyName, r.JobTitle, r.EmployedAt, r.SalaryBand,
        r.PublicStory, r.StoryText, r.CreatedAt);

    protected override BackendC.GraduateOutcomeRequest ToBackend(BffC.GraduateOutcomeRequest r) => new(
        r.UserId, r.CourseId, r.CompanyName, r.JobTitle, r.EmployedAt, r.SalaryBand, r.PublicStory, r.StoryText);
}
