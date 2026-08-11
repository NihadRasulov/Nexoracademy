package az.demo.NexoraAcademy.config;

import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Optional;

/**
 * Tətbiq hər dəfə işə düşəndə default admin-panel hesablarının (bir dənə hər rol üçün:
 * SYSTEM_ADMIN, ADMIN, SALES_CRM, CONTENT_MANAGER) mövcudluğunu yoxlayır. DB-də email üzrə
 * artıq varsa toxunmur, yoxdursa bir dəfəlik yaradır (bax UserRepository.existsByEmail).
 *
 * <p>Əvvəlki versiya yalnız tək bir "admin@nexora.com" (SYSTEM_ADMIN) seed edirdi — həmin
 * hesab artıq "system-admin@nexora.com" olaraq adlandırılıb, ona görə mövcud DB-lərdə köhnə
 * yazını da (email+şifrə) miqrasiya edirik ki, iki dəfə yaranmasın.
 *
 * <p><b>Təhlükəsizlik:</b> şifrələr əvvəllər bu sinifdə hardcode idi ("admin1234" və s.) və
 * seed hər profildə, o cümlədən prod-da işləyirdi — yəni kodu görən hər kəs canlı sistemə
 * SYSTEM_ADMIN kimi girə bilərdi. İndi dəyərlər {@link AdminSeederProperties}-dən gəlir:
 * dev-də rahat default-lar qalır, prod profilində isə seed default olaraq SÖNÜLÜDÜR və
 * yalnız real şifrələr env ilə verildikdə işə düşür (boş şifrə → tətbiq açılışda dayanır).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    /** Bundan qısa şifrələr bloklanmır, amma açılışda WARN ilə xəbərdarlıq edilir. */
    private static final int RECOMMENDED_MIN_PASSWORD_LENGTH = 12;

    private static final String LEGACY_SYSTEM_ADMIN_EMAIL = "admin@nexora.com";

    private static final String SYSTEM_ADMIN_EMAIL = "system-admin@nexora.com";
    private static final String ADMIN_EMAIL = "admin@nexora.com";
    private static final String SALES_CRM_EMAIL = "sales-crm@nexora.com";
    private static final String CONTENT_MANAGER_EMAIL = "content-manager@nexora.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminSeederProperties properties;

    @Override
    public void run(String... args) {
        if (!properties.isEnabled()) {
            log.info("app.admin-seed.enabled=false — default admin hesabları seed edilmir.");
            return;
        }

        requirePassword(properties.getSystemAdminPassword(), "system-admin-password", SYSTEM_ADMIN_EMAIL);
        requirePassword(properties.getAdminPassword(), "admin-password", ADMIN_EMAIL);
        requirePassword(properties.getSalesCrmPassword(), "sales-crm-password", SALES_CRM_EMAIL);
        requirePassword(properties.getContentManagerPassword(), "content-manager-password", CONTENT_MANAGER_EMAIL);

        migrateLegacySystemAdmin();

        seedIfMissing(SYSTEM_ADMIN_EMAIL, properties.getSystemAdminPassword(), UserRole.SYSTEM_ADMIN, "System", "Admin");
        seedIfMissing(ADMIN_EMAIL, properties.getAdminPassword(), UserRole.ADMIN, "Nexora", "Admin");
        seedIfMissing(SALES_CRM_EMAIL, properties.getSalesCrmPassword(), UserRole.SALES_CRM, "Sales", "CRM");
        seedIfMissing(CONTENT_MANAGER_EMAIL, properties.getContentManagerPassword(), UserRole.CONTENT_MANAGER, "Content", "Manager");
    }

    /**
     * Seed aktivdirsə şifrənin verilməsi məcburidir — prod-da default-lar boşdur, ona görə
     * ADMIN_SEED_ENABLED=true edilib şifrə verilməyibsə, hesabı "yaratmamaq" və ya təsadüfi
     * şifrə ilə yaratmaq əvəzinə açılışda dayanmaq daha təhlükəsizdir (fail-fast).
     */
    private void requirePassword(String password, String propertyName, String email) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalStateException(
                    "app.admin-seed.enabled=true, lakin app.admin-seed." + propertyName + " boşdur ("
                            + email + "). Ya şifrəni təyin et (ADMIN_SEED_* env dəyişənləri), "
                            + "ya da ADMIN_SEED_ENABLED=false ilə seed-i söndür.");
        }

        if (password.length() < RECOMMENDED_MIN_PASSWORD_LENGTH) {
            log.warn("{} üçün seed şifrəsi {} simvoldan qısadır — production-da güclü şifrə istifadə et.",
                    email, RECOMMENDED_MIN_PASSWORD_LENGTH);
        }
    }

    /**
     * Köhnə tək-hesab seed məntiqi "admin@nexora.com"-u SYSTEM_ADMIN rolu ilə yaradırdı.
     * İndi "admin@nexora.com" ADMIN roluna, "system-admin@nexora.com" isə SYSTEM_ADMIN roluna
     * aiddir — o yazı hələ DB-də köhnə email/rol kombinasiyası ilə qalıbsa, onu yeni email və
     * şifrəyə köçürürük ki, aşağıdakı seedIfMissing çağırışları rolları qarışdırmasın.
     */
    private void migrateLegacySystemAdmin() {
        if (userRepository.existsByEmail(SYSTEM_ADMIN_EMAIL)) {
            return;
        }

        Optional<User> legacy = userRepository.findByEmail(LEGACY_SYSTEM_ADMIN_EMAIL)
                .filter(user -> user.getRole() == UserRole.SYSTEM_ADMIN);

        legacy.ifPresent(user -> {
            user.setEmail(SYSTEM_ADMIN_EMAIL);
            user.setFirstName("System");
            user.setLastName("Admin");
            user.setPasswordHash(passwordEncoder.encode(properties.getSystemAdminPassword()));
            userRepository.save(user);
            log.info("Köhnə default admin hesabı miqrasiya edildi: {} -> {}", LEGACY_SYSTEM_ADMIN_EMAIL, SYSTEM_ADMIN_EMAIL);
        });
    }

    private void seedIfMissing(String email, String rawPassword, UserRole role, String firstName, String lastName) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            // Ensure previously-seeded admins have emailVerifiedAt set so they can log in
            if (user.getEmailVerifiedAt() == null) {
                user.setEmailVerifiedAt(Instant.now());
                userRepository.save(user);
                log.info("{} ({}) üçün emailVerifiedAt təyin edildi.", role, email);
            } else {
                log.info("{} ({}) artıq mövcuddur, seed edilmir.", role, email);
            }
            return;
        }

        user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);
        user.setEmailVerifiedAt(Instant.now());

        userRepository.save(user);
        log.info("Default {} hesabı yaradıldı: {}", role, email);
    }
}
