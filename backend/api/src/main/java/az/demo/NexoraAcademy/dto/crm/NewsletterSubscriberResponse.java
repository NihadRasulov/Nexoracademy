package az.demo.NexoraAcademy.dto.crm;

import java.time.Instant;
import java.util.UUID;

public record NewsletterSubscriberResponse(
        UUID id,
        String email,
        String consentVersion,
        Boolean active,
        Instant subscribedAt,
        Instant updatedAt) {
}
