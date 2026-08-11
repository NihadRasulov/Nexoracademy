namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record CmsContentRequest(
    string? Key, string? Type, string? Title, string? Body,
    Dictionary<string, object>? Data, bool? Published, int? SortOrder);

public record CmsContentResponse(
    long Id, string Key, string Type, string? Title, string? Body,
    Dictionary<string, object>? Data, bool Published, int SortOrder, Guid? UpdatedBy, DateTimeOffset UpdatedAt);
