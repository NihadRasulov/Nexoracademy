package az.demo.NexoraAcademy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Default admin-panel hesablarının seed konfiqurasiyası (bax {@link AdminSeeder}).
 *
 * <p>Şifrələr qəsdən koda hardcode edilmir: dev-də rahat olsun deyə
 * {@code application.yml}-da zəif default-lar var, prod profilində isə
 * {@code application-prod.yml} həm {@code enabled}-i {@code false} edir, həm də
 * bütün şifrə default-larını boş saxlayır — yəni prod-da seed yalnız
 * {@code ADMIN_SEED_ENABLED=true} + real şifrələr verildikdə işləyir.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.admin-seed")
public class AdminSeederProperties {

    /** false olduqda heç bir default hesab yaradılmır (prod default-u). */
    private boolean enabled = true;

    private String systemAdminPassword;

    private String adminPassword;

    private String salesCrmPassword;

    private String contentManagerPassword;
}
