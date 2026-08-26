namespace NexoraAcademy.AdminBff.Auth;

public static class Roles
{
    public const string Admin = "ADMIN";

    public static bool CanAccessPanel(string role) =>
        string.Equals(role, Admin, StringComparison.Ordinal);
}
