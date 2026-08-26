package az.demo.NexoraAcademy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Raw HMAC-SHA signing secret (UTF-8 bytes). Must be at least 32 characters (256 bits). */
    private String secret = "change-me-change-me-change-me-change-me-change-me!!";

    private String issuer = "nexora-academy";

    private long accessTokenExpirationMs = 15 * 60 * 1000L;            // 15 minutes

    private long refreshTokenExpirationMs = 30L * 24 * 60 * 60 * 1000; // 30 days
}
