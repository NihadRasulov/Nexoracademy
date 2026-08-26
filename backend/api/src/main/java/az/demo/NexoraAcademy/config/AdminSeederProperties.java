package az.demo.NexoraAcademy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.admin-seed")
public class AdminSeederProperties {
    private boolean enabled = true;
    private String email = "admin@nexora.com";
    private String password;
}
