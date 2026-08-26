package az.demo.NexoraAcademy.service.crm;

import az.demo.NexoraAcademy.dto.crm.NewsletterSubscriberResponse;
import az.demo.NexoraAcademy.dto.crm.NewsletterSubscriptionRequest;
import az.demo.NexoraAcademy.entity.crm.NewsletterSubscription;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.crm.NewsletterSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NewsletterSubscriptionService {
    private final NewsletterSubscriptionRepository repository;

    public void subscribe(NewsletterSubscriptionRequest request) {
        NewsletterSubscription subscription = repository.findByEmail(request.email())
                .orElseGet(NewsletterSubscription::new);
        subscription.setEmail(request.email());
        subscription.setConsentVersion(request.consentVersion());
        subscription.setActive(true);
        repository.save(subscription);
    }

    @Transactional(readOnly = true)
    public List<NewsletterSubscriberResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) throw ResourceNotFoundException.of("NewsletterSubscription", id);
        repository.deleteById(id);
    }

    private NewsletterSubscriberResponse toResponse(NewsletterSubscription subscription) {
        return new NewsletterSubscriberResponse(
                subscription.getId(), subscription.getEmail(), subscription.getConsentVersion(),
                subscription.getActive(), subscription.getSubscribedAt(), subscription.getUpdatedAt());
    }
}
