using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Controllers.Generic;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[Route("api/admin/sales/chat-sessions")]
[Authorize(Roles = Roles.SalesCrm)]
public class ChatSessionController(IChatSessionApiClient client)
    : BffCrudControllerBase<BackendC.ChatSessionResponse, BackendC.ChatSessionRequest, BffC.ChatSessionResponse, BffC.ChatSessionRequest, Guid>
{
    protected override IBackendCrudClient<BackendC.ChatSessionResponse, BackendC.ChatSessionRequest, Guid> Client => client;

    protected override BffC.ChatSessionResponse ToBff(BackendC.ChatSessionResponse r) =>
        new(r.Id, r.UserId, r.LeadId, r.Channel, r.Messages, r.StartedAt, r.EndedAt);

    protected override BackendC.ChatSessionRequest ToBackend(BffC.ChatSessionRequest r) =>
        new(r.UserId, r.LeadId, r.Channel, r.Messages);
}
