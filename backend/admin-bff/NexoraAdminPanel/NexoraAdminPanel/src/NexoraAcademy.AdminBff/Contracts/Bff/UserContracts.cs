namespace NexoraAcademy.AdminBff.Contracts.Bff;

public record UserResponse(
    Guid Id, string Email, string? Phone, string FullName, string Role, string Status,
    string Locale, Dictionary<string, object>? Profile, DateTimeOffset? LastLoginAt,
    DateTimeOffset CreatedAt, DateTimeOffset UpdatedAt);

public record UserRequest(
    string? Email, string? Phone, string? FullName, string? Password,
    string? Role, string? Status, string? Locale, Dictionary<string, object>? Profile);

public record UpdateProfileRequest(
    string? Email, string? Phone, string? FullName, string? Locale, Dictionary<string, object>? Profile);

public record ChangePasswordRequest(string CurrentPassword, string NewPassword);
