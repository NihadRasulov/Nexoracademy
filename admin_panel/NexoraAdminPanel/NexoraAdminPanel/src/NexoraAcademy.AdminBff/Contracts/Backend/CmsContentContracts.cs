using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record CmsContentRequest(
    [property: JsonPropertyName("key")] string? Key,
    [property: JsonPropertyName("type")] string? Type,
    [property: JsonPropertyName("title")] string? Title,
    [property: JsonPropertyName("body")] string? Body,
    [property: JsonPropertyName("data")] Dictionary<string, object>? Data,
    [property: JsonPropertyName("published")] bool? Published,
    [property: JsonPropertyName("sortOrder")] int? SortOrder);

public record CmsContentResponse(
    [property: JsonPropertyName("id")] long Id,
    [property: JsonPropertyName("key")] string Key,
    [property: JsonPropertyName("type")] string Type,
    [property: JsonPropertyName("title")] string? Title,
    [property: JsonPropertyName("body")] string? Body,
    [property: JsonPropertyName("data")] Dictionary<string, object>? Data,
    [property: JsonPropertyName("published")] bool Published,
    [property: JsonPropertyName("sortOrder")] int SortOrder,
    [property: JsonPropertyName("updatedBy")] Guid? UpdatedBy,
    [property: JsonPropertyName("updatedAt")] DateTimeOffset UpdatedAt);
