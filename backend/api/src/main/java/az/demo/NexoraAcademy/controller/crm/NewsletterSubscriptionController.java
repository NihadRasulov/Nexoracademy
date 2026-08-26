package az.demo.NexoraAcademy.controller.crm;

import az.demo.NexoraAcademy.dto.crm.NewsletterSubscriberResponse;
import az.demo.NexoraAcademy.service.crm.NewsletterSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/newsletter-subscribers")
@RequiredArgsConstructor
public class NewsletterSubscriptionController {
    private final NewsletterSubscriptionService service;

    @GetMapping
    public ResponseEntity<List<NewsletterSubscriberResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
