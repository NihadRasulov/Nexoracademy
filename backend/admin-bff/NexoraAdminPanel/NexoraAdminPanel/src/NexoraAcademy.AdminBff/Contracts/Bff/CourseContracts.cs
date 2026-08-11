namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record CourseRequest(
    string? Slug, short? CategoryId, string? Title, string? ShortDescription, string? FullDescription,
    string? TargetAudience, string? Difficulty, short? DurationWeeks, string? DeliveryFormat,
    string? LocationText, decimal? BasePrice, string? Currency, string? PricePeriod,
    bool? Published, bool? Active, bool? Archived, DateTimeOffset? ValidFrom, DateTimeOffset? ValidUntil,
    Dictionary<string, object>? Content, List<Guid>? RelatedCourseIds);

public record CourseResponse(
    Guid Id, string Slug, short CategoryId, string Title, string? ShortDescription, string? FullDescription,
    string? TargetAudience, string Difficulty, short? DurationWeeks, string DeliveryFormat,
    string? LocationText, decimal? BasePrice, string? Currency, string? PricePeriod,
    bool Published, bool Active, bool Archived, DateTimeOffset? ValidFrom, DateTimeOffset? ValidUntil,
    Dictionary<string, object>? Content, List<Guid>? RelatedCourseIds, Guid? CreatedBy,
    DateTimeOffset CreatedAt, DateTimeOffset UpdatedAt);
