using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record KbArticleRequest(
    [property: JsonPropertyName("sourceType")] string? SourceType,
    [property: JsonPropertyName("sourceRefId")] string? SourceRefId,
    [property: JsonPropertyName("title")] string? Title,
    [property: JsonPropertyName("content")] string? Content,
    [property: JsonPropertyName("active")] bool? Active);

public record KbArticleResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("sourceType")] string SourceType,
    [property: JsonPropertyName("sourceRefId")] string? SourceRefId,
    [property: JsonPropertyName("title")] string? Title,
    [property: JsonPropertyName("content")] string Content,
    [property: JsonPropertyName("active")] bool Active,
    [property: JsonPropertyName("updatedAt")] DateTimeOffset UpdatedAt);
