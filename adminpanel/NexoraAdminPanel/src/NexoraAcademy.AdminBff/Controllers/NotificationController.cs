using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/notifications")]
[Authorize(Roles = Roles.AdminOnly)]
public class NotificationController(INotificationApiClient client)
    : BffCrudControllerBase<BackendC.NotificationResponse, BackendC.NotificationRequest, BffC.NotificationResponse, BffC.NotificationRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.NotificationResponse, BackendC.NotificationRequest, Guid> Client => client;

    protected override BffC.NotificationResponse ToBff(BackendC.NotificationResponse r) => new(
        r.Id, r.UserId, r.Type, r.Channel, r.Payload, r.Status, r.SentAt, r.ReadAt, r.CreatedAt);

    protected override BackendC.NotificationRequest ToBackend(BffC.NotificationRequest r) => new(
        r.UserId, r.Type, r.Channel, r.Payload);
}
