using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface INotificationApiClient : IBackendCrudClient<NotificationResponse, NotificationRequest, Guid>;

public class NotificationApiClient(HttpClient httpClient)
    : BackendCrudClient<NotificationResponse, NotificationRequest, Guid>(httpClient, "/api/v1/notifications"), INotificationApiClient;
