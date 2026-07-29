using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record ScholarshipRequest(
    [property: JsonPropertyName("name")] string? Name,
    [property: JsonPropertyName("description")] string? Description,
    [property: JsonPropertyName("discountPct")] decimal? DiscountPct,
    [property: JsonPropertyName("maxRecipients")] int? MaxRecipients,
    [property: JsonPropertyName("validFrom")] DateOnly? ValidFrom,
    [property: JsonPropertyName("validUntil")] DateOnly? ValidUntil,
    [property: JsonPropertyName("active")] bool? Active);

public record ScholarshipResponse(
    [property: JsonPropertyName("id")] short Id,
    [property: JsonPropertyName("name")] string Name,
    [property: JsonPropertyName("description")] string? Description,
    [property: JsonPropertyName("discountPct")] decimal? DiscountPct,
    [property: JsonPropertyName("maxRecipients")] int? MaxRecipients,
    [property: JsonPropertyName("validFrom")] DateOnly? ValidFrom,
    [property: JsonPropertyName("validUntil")] DateOnly? ValidUntil,
    [property: JsonPropertyName("active")] bool Active,
    [property: JsonPropertyName("applications")] List<Dictionary<string, object>>? Applications);
