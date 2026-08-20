namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record ApplicationRequest(
    short ApplicationType,
    string Fullname,
    string Email,
    string Phone,
    string Letter);

public record ApplicationResponse(
    long Id,
    short ApplicationType,
    string Fullname,
    string Email,
    string Phone,
    string Letter,
    string? CvFilename,
    string Status,
    DateTimeOffset CreatedAt);
