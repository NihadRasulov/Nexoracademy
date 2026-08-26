package az.demo.NexoraAcademy.dto.billing;

import az.demo.NexoraAcademy.entity.enums.PaymentMethod;
import az.demo.NexoraAcademy.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID enrollmentId,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String externalTxnId,
        String idempotencyKey,
        List<Map<String, Object>> installments,
        BigDecimal refundAmount,
        String refundReason,
        Instant initiatedAt,
        Instant capturedAt,
        String failureReason
) {
}
