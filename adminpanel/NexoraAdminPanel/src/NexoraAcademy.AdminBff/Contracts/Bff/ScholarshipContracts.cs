namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record ScholarshipRequest(
    string? Name, string? Description, decimal? DiscountPct, int? MaxRecipients,
    DateOnly? ValidFrom, DateOnly? ValidUntil, bool? Active);

public record ScholarshipResponse(
    short Id, string Name, string? Description, decimal? DiscountPct, int? MaxRecipients,
    DateOnly? ValidFrom, DateOnly? ValidUntil, bool Active, List<Dictionary<string, object>>? Applications);
