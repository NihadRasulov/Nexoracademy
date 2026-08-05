using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/audit-logs")]
[Authorize(Roles = Roles.AdminOnly)]
public class AuditLogController(IAuditLogApiClient client)
    : BffCrudControllerBase<BackendC.AuditLogResponse, BackendC.AuditLogRequest, BffC.AuditLogResponse, BffC.AuditLogRequest, long>
{
    protected override IBackendCrudClient<BackendC.AuditLogResponse, BackendC.AuditLogRequest, long> Client => client;

    protected override BffC.AuditLogResponse ToBff(BackendC.AuditLogResponse r) => new(
        r.Id, r.ActorId, r.Action, r.EntityType, r.EntityId, r.BeforeState, r.AfterState,
        r.TraceId, r.IpAddress, r.CreatedAt);

    protected override BackendC.AuditLogRequest ToBackend(BffC.AuditLogRequest r) => new(
        r.ActorId, r.Action, r.EntityType, r.EntityId, r.BeforeState, r.AfterState, r.IpAddress);
}
