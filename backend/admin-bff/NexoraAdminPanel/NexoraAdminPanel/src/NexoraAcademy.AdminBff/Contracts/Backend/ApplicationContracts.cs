using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record ApplicationRequest(
    [property: JsonPropertyName("applicationType")] short ApplicationType,
    [property: JsonPropertyName("fullname")] string Fullname,
    [property: JsonPropertyName("email")] string Email,
    [property: JsonPropertyName("phone")] string Phone,
    [property: JsonPropertyName("letter")] string Letter);

public record ApplicationResponse(
    [property: JsonPropertyName("id")] long Id,
    [property: JsonPropertyName("applicationType")] short ApplicationType,
    [property: JsonPropertyName("fullname")] string Fullname,
    [property: JsonPropertyName("email")] string Email,
    [property: JsonPropertyName("phone")] string Phone,
    [property: JsonPropertyName("letter")] string Letter,
    [property: JsonPropertyName("cvFilename")] string? CvFilename,
    [property: JsonPropertyName("status")] string Status,
    [property: JsonPropertyName("createdAt")] DateTimeOffset CreatedAt);
