using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IChatSessionApiClient : IBackendCrudClient<ChatSessionResponse, ChatSessionRequest, Guid>;

public class ChatSessionApiClient(HttpClient httpClient)
    : BackendCrudClient<ChatSessionResponse, ChatSessionRequest, Guid>(httpClient, "/api/v1/sales/chat-sessions"), IChatSessionApiClient;
