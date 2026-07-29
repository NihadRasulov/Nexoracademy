namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record ErrorResponse(
    string Code,
    string Message,
    Dictionary<string, string>? FieldErrors = null);
