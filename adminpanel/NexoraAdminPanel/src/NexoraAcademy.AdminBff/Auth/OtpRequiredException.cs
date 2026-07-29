namespace NexoraAcademy.AdminBff.Auth;

public class OtpRequiredException(string email) : Exception(
    $"Login ucun OTP telep olundu, amma bu hesab admin panelinden istifade etmemelidir (email={email}).");
