package az.demo.NexoraAcademy.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent(UUID paymentId, UUID enrollmentId, UUID userId, BigDecimal amount, String currency) {
}
