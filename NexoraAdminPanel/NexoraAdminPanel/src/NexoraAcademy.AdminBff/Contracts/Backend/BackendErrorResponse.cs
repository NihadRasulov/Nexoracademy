using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

public record BackendErrorResponse(
    [property: JsonPropertyName("timestamp")] DateTimeOffset Timestamp,
    [property: JsonPropertyName("status")] int Status,
    [property: JsonPropertyName("error")] string Error,
    [property: JsonPropertyName("message")] string Message,
    [property: JsonPropertyName("path")] string Path,
    [property: JsonPropertyName("errors")] Dictionary<string, string>? Errors = null);
