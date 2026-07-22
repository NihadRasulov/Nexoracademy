package az.demo.NexoraAcademy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private long passwordResetExpirationMs = 60 * 60 * 1000L;      // 1 hour

    private long emailVerifyExpirationMs = 24 * 60 * 60 * 1000L;   // 24 hours
}
