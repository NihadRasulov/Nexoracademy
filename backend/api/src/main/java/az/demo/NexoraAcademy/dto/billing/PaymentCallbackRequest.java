package az.demo.NexoraAcademy.dto.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload a payment gateway posts back to confirm/deny a payment it was
 * asked to process. Payments are matched by the idempotency key the gateway
 * was given when the payment was created — not by our internal UUID —
 * since the gateway only ever sees the key it was handed.
 */
public record PaymentCallbackRequest(
        @NotBlank @Size(max = 100) String idempotencyKey,

        @NotBlank
        @Pattern(regexp = "^(captured|failed)$", message = "status must be 'captured' or 'failed'")
        String status,

        @Size(max = 150) String externalTxnId,

        @Size(max = 255) String failureReason
) {
}
