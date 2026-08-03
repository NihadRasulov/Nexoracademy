namespace NexoraAcademy.AdminBff.Auth;

public static class Roles
{
    public const string AdminOnly = "ADMIN,SYSTEM_ADMIN";
    public const string ContentManager = "ADMIN,SYSTEM_ADMIN,CONTENT_MANAGER";
    public const string SalesCrm = "ADMIN,SYSTEM_ADMIN,SALES_CRM";
}
