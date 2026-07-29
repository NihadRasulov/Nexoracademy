using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record BackendPage<T>(
    [property: JsonPropertyName("content")] List<T> Content,
    [property: JsonPropertyName("totalElements")] long TotalElements,
    [property: JsonPropertyName("totalPages")] int TotalPages,
    [property: JsonPropertyName("number")] int Number,
    [property: JsonPropertyName("size")] int Size);
