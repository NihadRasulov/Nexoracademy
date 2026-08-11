using NexoraAcademy.AdminBff.Clients.Generic;
using NexoraAcademy.AdminBff.Contracts.Backend;

namespace NexoraAcademy.AdminBff.Clients;

public interface ICategoryApiClient : IBackendCrudClient<CategoryResponse, CategoryRequest, short>;

public class CategoryApiClient(HttpClient httpClient)
    : BackendCrudClient<CategoryResponse, CategoryRequest, short>(httpClient, "/api/v1/categories"), ICategoryApiClient;
