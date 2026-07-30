using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/sales/contact-submissions")]
[Authorize(Roles = Roles.SalesCrm)]
public class ContactSubmissionController(IContactSubmissionApiClient client)
    : BffCrudControllerBase<BackendC.ContactSubmissionResponse, BackendC.ContactSubmissionRequest, BffC.ContactSubmissionResponse, BffC.ContactSubmissionRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.ContactSubmissionResponse, BackendC.ContactSubmissionRequest, Guid> Client => client;

    protected override BffC.ContactSubmissionResponse ToBff(BackendC.ContactSubmissionResponse r) => new(
        r.Id, r.LeadId, r.Type, r.CourseId, r.FullName, r.Email, r.Phone, r.Message, r.PreferredTime, r.Status, r.SubmittedAt);

    protected override BackendC.ContactSubmissionRequest ToBackend(BffC.ContactSubmissionRequest r) => new(
        r.LeadId, r.Type, r.CourseId, r.FullName, r.Email, r.Phone, r.Message, r.PreferredTime);
}
