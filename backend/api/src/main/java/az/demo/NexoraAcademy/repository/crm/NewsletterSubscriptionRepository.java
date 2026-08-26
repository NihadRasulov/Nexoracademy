package az.demo.NexoraAcademy.repository.crm;

import az.demo.NexoraAcademy.entity.crm.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, UUID> {
    Optional<NewsletterSubscription> findByEmail(String email);
    long countByActiveTrue();
}
