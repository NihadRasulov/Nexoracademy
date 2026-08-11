using System.Text.RegularExpressions;

namespace NexoraAcademy.AdminBff.Configuration;

public sealed record AdminSettings
{
    public const string SectionName = "AdminSettings";

    private static readonly Regex SecretPathPattern = new(
        "^[a-z0-9](?:[a-z0-9-]{10,62}[a-z0-9])$",
        RegexOptions.CultureInvariant | RegexOptions.NonBacktracking);

    private static readonly HashSet<string> ReservedPaths = new(StringComparer.OrdinalIgnoreCase)
    {
        "admin",
        "administrator",
        "api",
        "login"
    };

    public string SecretPath { get; init; } = string.Empty;
    public int SessionIdleTimeoutMinutes { get; init; } = 480;
    public LoginRateLimitSettings LoginRateLimit { get; init; } = new();

    public PathString BasePath => new($"/{SecretPath}");

    public static AdminSettings Load(IConfiguration configuration)
    {
        var settings = configuration.GetRequiredSection(SectionName).Get<AdminSettings>()
            ?? throw new InvalidOperationException($"{SectionName} konfiqurasiyasi tapilmadi.");

        if (string.IsNullOrWhiteSpace(settings.SecretPath)
            || settings.SecretPath != settings.SecretPath.Trim()
            || !SecretPathPattern.IsMatch(settings.SecretPath)
            || ReservedPaths.Contains(settings.SecretPath))
        {
            throw new InvalidOperationException(
                $"{SectionName}:SecretPath 12-64 simvoldan ibaret, kicik herfli, " +
                "reqem ve defislerden qurulmus tek URL segmenti olmalidir.");
        }

        if (settings.LoginRateLimit.PermitLimit is < 1 or > 100)
        {
            throw new InvalidOperationException(
                $"{SectionName}:LoginRateLimit:PermitLimit 1-100 araliginda olmalidir.");
        }

        if (settings.SessionIdleTimeoutMinutes is < 15 or > 1440)
        {
            throw new InvalidOperationException(
                $"{SectionName}:SessionIdleTimeoutMinutes 15-1440 araliginda olmalidir.");
        }

        if (settings.LoginRateLimit.WindowSeconds is < 1 or > 3600)
        {
            throw new InvalidOperationException(
                $"{SectionName}:LoginRateLimit:WindowSeconds 1-3600 araliginda olmalidir.");
        }

        return settings;
    }
}

public sealed record LoginRateLimitSettings
{
    public int PermitLimit { get; init; } = 5;
    public int WindowSeconds { get; init; } = 60;
}
