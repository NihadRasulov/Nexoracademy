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

    /** Registration email-verification OTP validity — short window, it's a 6-digit code now, not a link. */
    private long emailVerifyExpirationMs = 10 * 60 * 1000L;        // 10 minutes

    /** Post-password login OTP validity. */
    private long loginOtpExpirationMs = 10 * 60 * 1000L;           // 10 minutes

    /** Failed OTP guesses allowed before the code is revoked and a new one must be requested. */
    private int otpMaxAttempts = 5;
}
