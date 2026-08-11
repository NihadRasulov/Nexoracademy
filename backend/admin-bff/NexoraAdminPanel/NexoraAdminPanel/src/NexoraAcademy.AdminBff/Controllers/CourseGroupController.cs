using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/course-groups")]
[Authorize(Roles = Roles.ContentManager)]
public class CourseGroupController(ICourseGroupApiClient client)
    : BffCrudControllerBase<BackendC.CourseGroupResponse, BackendC.CourseGroupRequest, BffC.CourseGroupResponse, BffC.CourseGroupRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.CourseGroupResponse, BackendC.CourseGroupRequest, Guid> Client => client;

    protected override BffC.CourseGroupResponse ToBff(BackendC.CourseGroupResponse r) => new(
        r.Id, r.CourseId, r.GroupCode, r.StartDate, r.EndDate, r.RegistrationDeadline,
        r.TotalSeats, r.ReservedSeats, r.Status, r.Schedule, r.CreatedAt);

    protected override BackendC.CourseGroupRequest ToBackend(BffC.CourseGroupRequest r) => new(
        r.CourseId, r.GroupCode, r.StartDate, r.EndDate, r.RegistrationDeadline, r.TotalSeats, r.Status, r.Schedule);
}
