using System.Text.Json.Serialization;

namespace NexoraAcademy.AdminBff.Contracts.Backend;

// Member names intentionally mirror the Java API's serialized enum values.
#pragma warning disable CA1707
[JsonConverter(typeof(JsonStringEnumConverter))]
public enum UserRole
{
    GUEST,
    STUDENT,
    SALES_CRM,
    CONTENT_MANAGER,
    ADMIN,
    SYSTEM_ADMIN
}

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum AccountStatus
{
    PENDING_VERIFICATION,
    ACTIVE,
    SUSPENDED,
    BANNED,
    DEACTIVATED
}

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum EnrollmentStatus
{
    WAITLISTED,
    HELD,
    PENDING_PAYMENT,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    REFUNDED
}
#pragma warning restore CA1707
