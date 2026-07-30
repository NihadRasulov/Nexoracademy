using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/instructors")]
[Authorize(Roles = Roles.ContentManager)]
public class InstructorController(IInstructorApiClient client)
    : BffCrudControllerBase<BackendC.InstructorResponse, BackendC.InstructorRequest, BffC.InstructorResponse, BffC.InstructorRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.InstructorResponse, BackendC.InstructorRequest, Guid> Client => client;

    protected override BffC.InstructorResponse ToBff(BackendC.InstructorResponse r) =>
        new(r.Id, r.UserId, r.FullName, r.Bio, r.PhotoUrl, r.LinkedinUrl, r.AvgRating, r.Certifications, r.Active, r.CreatedAt);

    protected override BackendC.InstructorRequest ToBackend(BffC.InstructorRequest r) =>
        new(r.UserId, r.FullName, r.Bio, r.PhotoUrl, r.LinkedinUrl, r.Certifications, r.Active);
}
