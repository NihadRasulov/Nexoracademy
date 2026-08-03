using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/course-reviews")]
[Authorize(Roles = Roles.ContentManager)]
public class CourseReviewController(ICourseReviewApiClient client)
    : BffCrudControllerBase<BackendC.CourseReviewResponse, BackendC.CourseReviewRequest, BffC.CourseReviewResponse, BffC.CourseReviewRequest, long>
{
    protected override IBackendCrudClient<BackendC.CourseReviewResponse, BackendC.CourseReviewRequest, long> Client => client;

    protected override BffC.CourseReviewResponse ToBff(BackendC.CourseReviewResponse r) => new(
        r.Id, r.CourseId, r.UserId, r.EnrollmentId, r.Rating, r.Comment, r.Published,
        r.ModeratedBy, r.AiSentiment, r.CreatedAt);

    protected override BackendC.CourseReviewRequest ToBackend(BffC.CourseReviewRequest r) => new(
        r.CourseId, r.UserId, r.EnrollmentId, r.Rating, r.Comment);
}
