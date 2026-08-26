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

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminSeederProperties properties;

    @Override
    public void run(String... args) {
        if (!properties.isEnabled()) return;
        if (!StringUtils.hasText(properties.getEmail()) || !StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException(
                    "Admin seed enabled, but ADMIN_SEED_EMAIL or ADMIN_SEED_PASSWORD is empty");
        }
        if (properties.getPassword().length() < 12) {
            log.warn("Default admin password is shorter than the recommended 12 characters.");
        }

        User admin = userRepository.findByEmail(properties.getEmail()).orElseGet(User::new);
        admin.setEmail(properties.getEmail());
        admin.setFirstName("Nexora");
        admin.setLastName("Admin");
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        if (admin.getPasswordHash() == null) {
            admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        }
        userRepository.save(admin);
        log.info("CMS admin account is ready: {}", properties.getEmail());
    }
}
