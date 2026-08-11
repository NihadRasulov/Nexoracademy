using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface IContactSubmissionApiClient : IBackendCrudClient<ContactSubmissionResponse, ContactSubmissionRequest, Guid>;

public class ContactSubmissionApiClient(HttpClient httpClient)
    : BackendCrudClient<ContactSubmissionResponse, ContactSubmissionRequest, Guid>(httpClient, "/api/v1/sales/contact-submissions"),
        IContactSubmissionApiClient;
