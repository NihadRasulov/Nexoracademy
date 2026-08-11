namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record PagedResult<T>(List<T> Items, long TotalElements, int TotalPages, int Page, int Size);
