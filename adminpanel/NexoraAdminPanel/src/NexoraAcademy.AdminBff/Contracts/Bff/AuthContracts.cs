using System.ComponentModel.DataAnnotations;

namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record LoginRequest(
    [Required, EmailAddress] string Email,
    [Required] string Password);

public record MeResponse(
    Guid Id,
    string Email,
    string FullName,
    string? Phone,
    string Role,
    string Status,
    string Locale,
    DateTimeOffset? LastLoginAt);
