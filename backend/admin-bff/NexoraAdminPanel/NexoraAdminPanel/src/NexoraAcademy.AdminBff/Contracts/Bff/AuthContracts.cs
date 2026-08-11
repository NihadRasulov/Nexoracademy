using System.ComponentModel.DataAnnotations;

namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record LoginRequest(
    [Required, EmailAddress, StringLength(254)] string Email,
    [Required, StringLength(256)] string Password);

public record MeResponse(
    Guid Id,
    string Email,
    string FullName,
    string? Phone,
    string Role,
    string Status,
    string Locale,
    DateTimeOffset? LastLoginAt);
