using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IAuditLogApiClient : IBackendCrudClient<AuditLogResponse, AuditLogRequest, long>;

public class AuditLogApiClient(HttpClient httpClient)
    : BackendCrudClient<AuditLogResponse, AuditLogRequest, long>(httpClient, "/api/v1/admin/audit-logs"), IAuditLogApiClient;
