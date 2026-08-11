using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record CampaignRequest(
    [property: JsonPropertyName("name")] string? Name,
    [property: JsonPropertyName("bannerImageUrl")] string? BannerImageUrl,
    [property: JsonPropertyName("ctaUrl")] string? CtaUrl,
    [property: JsonPropertyName("discountPct")] decimal? DiscountPct,
    [property: JsonPropertyName("startsAt")] DateTimeOffset? StartsAt,
    [property: JsonPropertyName("endsAt")] DateTimeOffset? EndsAt,
    [property: JsonPropertyName("active")] bool? Active,
    [property: JsonPropertyName("priority")] int? Priority,
    [property: JsonPropertyName("courseIds")] List<Guid>? CourseIds);

public record CampaignResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("name")] string Name,
    [property: JsonPropertyName("bannerImageUrl")] string? BannerImageUrl,
    [property: JsonPropertyName("ctaUrl")] string? CtaUrl,
    [property: JsonPropertyName("discountPct")] decimal? DiscountPct,
    [property: JsonPropertyName("startsAt")] DateTimeOffset StartsAt,
    [property: JsonPropertyName("endsAt")] DateTimeOffset EndsAt,
    [property: JsonPropertyName("active")] bool Active,
    [property: JsonPropertyName("priority")] int Priority,
    [property: JsonPropertyName("courseIds")] List<Guid>? CourseIds);
