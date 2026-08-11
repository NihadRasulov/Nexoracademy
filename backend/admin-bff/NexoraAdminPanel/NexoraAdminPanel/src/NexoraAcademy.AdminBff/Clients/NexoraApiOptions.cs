namespace NexoraAcademy.AdminBff.Clients;

public class NexoraApiOptions
{
    public const string SectionName = "NexoraApi";

    public required string BaseUrl { get; set; }
    public int TimeoutSeconds { get; set; } = 30;
}
