package az.demo.NexoraAcademy.repository.identity;

import az.demo.NexoraAcademy.entity.enums.OAuthProvider;
import az.demo.NexoraAcademy.entity.identity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
}
