package az.demo.NexoraAcademy.controller.publicapi;

import az.demo.NexoraAcademy.dto.crm.NewsletterSubscriptionRequest;
import az.demo.NexoraAcademy.dto.crm.NewsletterSubscriptionResponse;
import az.demo.NexoraAcademy.dto.crm.PublicContactSubmissionRequest;
import az.demo.NexoraAcademy.entity.enums.SubmissionType;
import az.demo.NexoraAcademy.service.crm.ContactSubmissionService;
import az.demo.NexoraAcademy.service.crm.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicEngagementController {
    private final LeadService leadService;
    private final ContactSubmissionService contactSubmissionService;

    @PostMapping("/newsletter/subscriptions")
    public ResponseEntity<NewsletterSubscriptionResponse> subscribe(@Valid @RequestBody NewsletterSubscriptionRequest request) {
        leadService.subscribe(request);
        return ResponseEntity.accepted().body(new NewsletterSubscriptionResponse("Subscription recorded."));
    }

    @PostMapping("/contact-submissions")
    public ResponseEntity<Void> contact(@Valid @RequestBody PublicContactSubmissionRequest request) {
        contactSubmissionService.create(new az.demo.NexoraAcademy.dto.crm.ContactSubmissionRequest(
                null, SubmissionType.CONTACT, request.courseId(), request.fullName(), request.email(),
                request.phone(), request.message(), null));
        return ResponseEntity.accepted().build();
    }
}
