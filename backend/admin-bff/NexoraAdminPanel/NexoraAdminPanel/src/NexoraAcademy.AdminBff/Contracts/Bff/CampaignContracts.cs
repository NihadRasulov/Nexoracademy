namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record CampaignRequest(
    string? Name, string? BannerImageUrl, string? CtaUrl, decimal? DiscountPct,
    DateTimeOffset? StartsAt, DateTimeOffset? EndsAt, bool? Active, int? Priority, List<Guid>? CourseIds);

public record CampaignResponse(
    Guid Id, string Name, string? BannerImageUrl, string? CtaUrl, decimal? DiscountPct,
    DateTimeOffset StartsAt, DateTimeOffset EndsAt, bool Active, int Priority, List<Guid>? CourseIds);
