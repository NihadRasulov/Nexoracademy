using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

// Member names intentionally mirror the Java API's serialized enum values.
#pragma warning disable CA1707
[JsonConverter(typeof(JsonStringEnumConverter))]
public enum UserRole
{
    ADMIN
}

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum AccountStatus
{
    ACTIVE,
    DEACTIVATED
}
#pragma warning restore CA1707
