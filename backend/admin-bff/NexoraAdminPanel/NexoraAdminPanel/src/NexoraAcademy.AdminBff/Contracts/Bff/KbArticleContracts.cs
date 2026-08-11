namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record KbArticleRequest(string? SourceType, string? SourceRefId, string? Title, string? Content, bool? Active);

public record KbArticleResponse(
    Guid Id, string SourceType, string? SourceRefId, string? Title, string Content, bool Active, DateTimeOffset UpdatedAt);
