namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record CategoryRequest(string? Slug, string? Name, short? ParentId, int? SortOrder, bool? Active);

public record CategoryResponse(short Id, string Slug, string Name, short? ParentId, int SortOrder, bool Active);
