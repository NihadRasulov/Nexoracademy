using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record UserResponse(
    [property: JsonPropertyName("id")] Guid Id,
    [property: JsonPropertyName("email")] string Email,
    [property: JsonPropertyName("phone")] string? Phone,
    [property: JsonPropertyName("fullName")] string FullName,
    [property: JsonPropertyName("role")] UserRole Role,
    [property: JsonPropertyName("status")] AccountStatus Status,
    [property: JsonPropertyName("locale")] string Locale,
    [property: JsonPropertyName("profile")] Dictionary<string, object>? Profile,
    [property: JsonPropertyName("lastLoginAt")] DateTimeOffset? LastLoginAt,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt,
    [property: JsonPropertyName("updatedAt")] DateTimeOffset UpdatedAt);

public record UserRequest(
    [property: JsonPropertyName("email")] string? Email,
    [property: JsonPropertyName("phone")] string? Phone,
    [property: JsonPropertyName("fullName")] string? FullName,
    [property: JsonPropertyName("password")] string? Password,
    [property: JsonPropertyName("role")] UserRole? Role,
    [property: JsonPropertyName("status")] AccountStatus? Status,
    [property: JsonPropertyName("locale")] string? Locale,
    [property: JsonPropertyName("profile")] Dictionary<string, object>? Profile);

public record UpdateProfileRequest(
    [property: JsonPropertyName("email")] string? Email,
    [property: JsonPropertyName("phone")] string? Phone,
    [property: JsonPropertyName("fullName")] string? FullName,
    [property: JsonPropertyName("locale")] string? Locale,
    [property: JsonPropertyName("profile")] Dictionary<string, object>? Profile);

public record ChangePasswordRequest(
    [property: JsonPropertyName("currentPassword")] string CurrentPassword,
    [property: JsonPropertyName("newPassword")] string NewPassword);
