using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record CategoryRequest(
    [property: JsonPropertyName("slug")] string? Slug,
    [property: JsonPropertyName("name")] string? Name,
    [property: JsonPropertyName("parentId")] short? ParentId,
    [property: JsonPropertyName("sortOrder")] int? SortOrder,
    [property: JsonPropertyName("active")] bool? Active);

public record CategoryResponse(
    [property: JsonPropertyName("id")] short Id,
    [property: JsonPropertyName("slug")] string Slug,
    [property: JsonPropertyName("name")] string Name,
    [property: JsonPropertyName("parentId")] short? ParentId,
    [property: JsonPropertyName("sortOrder")] int SortOrder,
    [property: JsonPropertyName("active")] bool Active);
