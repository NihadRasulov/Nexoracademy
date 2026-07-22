package az.demo.NexoraAcademy.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
    public static TokenResponse bearer(String accessToken, String refreshToken, long accessExpirationMs) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", accessExpirationMs / 1000);
    }
}
