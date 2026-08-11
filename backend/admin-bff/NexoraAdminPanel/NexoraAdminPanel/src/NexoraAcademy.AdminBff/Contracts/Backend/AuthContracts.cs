using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record LoginRequest(
    [property: JsonPropertyName("email")] string Email,
    [property: JsonPropertyName("password")] string Password);

public record RefreshTokenRequest(
    [property: JsonPropertyName("refreshToken")] string RefreshToken);

public record LoginResponse(
    [property: JsonPropertyName("accessToken")] string? AccessToken,
    [property: JsonPropertyName("refreshToken")] string? RefreshToken,
    [property: JsonPropertyName("tokenType")] string? TokenType,
    [property: JsonPropertyName("expiresInSeconds")] long? ExpiresInSeconds,
    [property: JsonPropertyName("message")] string? Message,
    [property: JsonPropertyName("email")] string? Email);

public record TokenResponse(
    [property: JsonPropertyName("accessToken")] string AccessToken,
    [property: JsonPropertyName("refreshToken")] string RefreshToken,
    [property: JsonPropertyName("tokenType")] string TokenType,
    [property: JsonPropertyName("expiresInSeconds")] long ExpiresInSeconds);
