namespace NexoraAcademy.AdminBff.Auth;

public static class Roles
{
    private static readonly HashSet<string> PanelRoles = new(StringComparer.Ordinal)
    {
        "ADMIN",
        "SYSTEM_ADMIN",
        "CONTENT_MANAGER",
        "SALES_CRM"
    };

    public const string PanelAccess = "ADMIN,SYSTEM_ADMIN,CONTENT_MANAGER,SALES_CRM";
    public const string AdminOnly = "ADMIN,SYSTEM_ADMIN";
    public const string ContentManager = "ADMIN,SYSTEM_ADMIN,CONTENT_MANAGER";
    public const string SalesCrm = "ADMIN,SYSTEM_ADMIN,SALES_CRM";

    public static bool CanAccessPanel(string role) => PanelRoles.Contains(role);
}
