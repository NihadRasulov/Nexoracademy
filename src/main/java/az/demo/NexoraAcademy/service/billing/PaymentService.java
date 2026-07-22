package az.demo.NexoraAcademy.service.billing;

import az.demo.NexoraAcademy.dto.billing.PaymentCallbackRequest;
import az.demo.NexoraAcademy.dto.billing.PaymentRequest;
import az.demo.NexoraAcademy.dto.billing.PaymentResponse;
import az.demo.NexoraAcademy.entity.academics.Enrollment;
import az.demo.NexoraAcademy.entity.billing.Payment;
import az.demo.NexoraAcademy.entity.enums.PaymentStatus;
import az.demo.NexoraAcademy.event.PaymentCompletedEvent;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.InvalidStateException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.academics.EnrollmentRepository;
import az.demo.NexoraAcademy.repository.billing.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public PaymentResponse create(PaymentRequest request) {
        assertIdempotencyKeyAvailable(request.idempotencyKey(), null);

        Payment payment = new Payment();
        payment.setEnrollment(resolveEnrollment(request.enrollmentId()));
        payment.setMethod(request.method());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency() != null ? request.currency() : "AZN");
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setExternalTxnId(request.externalTxnId());
        payment.setIdempotencyKey(request.idempotencyKey());
        payment.setInstallments(request.installments() != null ? request.installments() : new ArrayList<>());
        payment.setRefundAmount(BigDecimal.ZERO);

        return toResponse(paymentRepository.saveAndFlush(payment));
    }

    public PaymentResponse update(UUID id, PaymentRequest request) {
        Payment payment = getOrThrow(id);
        assertIdempotencyKeyAvailable(request.idempotencyKey(), id);

        payment.setEnrollment(resolveEnrollment(request.enrollmentId()));
        payment.setMethod(request.method());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency() != null ? request.currency() : payment.getCurrency());
        payment.setExternalTxnId(request.externalTxnId());
        payment.setIdempotencyKey(request.idempotencyKey());
        payment.setInstallments(request.installments() != null ? request.installments() : payment.getInstallments());

        return toResponse(paymentRepository.saveAndFlush(payment));
    }

    public PaymentResponse patch(UUID id, PaymentRequest request) {
        Payment payment = getOrThrow(id);

        if (request.enrollmentId() != null) payment.setEnrollment(resolveEnrollment(request.enrollmentId()));
        if (request.method() != null) payment.setMethod(request.method());
        if (request.amount() != null) payment.setAmount(request.amount());
        if (request.currency() != null) payment.setCurrency(request.currency());
        if (request.externalTxnId() != null) payment.setExternalTxnId(request.externalTxnId());
        if (request.idempotencyKey() != null) {
            assertIdempotencyKeyAvailable(request.idempotencyKey(), id);
            payment.setIdempotencyKey(request.idempotencyKey());
        }
        if (request.installments() != null) payment.setInstallments(request.installments());

        return toResponse(paymentRepository.saveAndFlush(payment));
    }

    /** Marks a payment captured — a common enough transition to expose as its own operation. */
    public PaymentResponse capture(UUID id) {
        Payment payment = getOrThrow(id);
        if (payment.getStatus() != PaymentStatus.INITIATED && payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new InvalidStateException("Payment " + id + " cannot be captured from status " + payment.getStatus());
        }
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setCapturedAt(Instant.now());
        Payment saved = paymentRepository.saveAndFlush(payment);

        eventPublisher.publishEvent(new PaymentCompletedEvent(
                saved.getId(), saved.getEnrollment().getId(), saved.getEnrollment().getUser().getId(),
                saved.getAmount(), saved.getCurrency()));

        return toResponse(saved);
    }

    /**
     * Entry point for a payment gateway's asynchronous callback/webhook.
     * Payments are matched by idempotency key (the only identifier the
     * gateway was ever given), not by our internal UUID.
     */
    public PaymentResponse handleCallback(PaymentCallbackRequest request) {
        Payment payment = paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with idempotencyKey: " + request.idempotencyKey()));

        if (request.externalTxnId() != null) {
            payment.setExternalTxnId(request.externalTxnId());
        }

        if ("captured".equals(request.status())) {
            return capture(payment.getId());
        }

        // "failed"
        if (payment.getStatus() != PaymentStatus.INITIATED && payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new InvalidStateException("Payment " + payment.getId() + " cannot be marked failed from status " + payment.getStatus());
        }
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(request.failureReason());
        return toResponse(paymentRepository.saveAndFlush(payment));
    }

    /** Records a refund against a captured payment. */
    public PaymentResponse refund(UUID id, BigDecimal refundAmount, String reason) {
        Payment payment = getOrThrow(id);
        if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new InvalidStateException("Payment " + id + " cannot be refunded from status " + payment.getStatus());
        }
        BigDecimal totalRefunded = payment.getRefundAmount().add(refundAmount);
        if (totalRefunded.compareTo(payment.getAmount()) > 0) {
            throw new InvalidStateException("Refund amount exceeds the paid amount for payment " + id);
        }
        payment.setRefundAmount(totalRefunded);
        payment.setRefundReason(reason);
        payment.setStatus(totalRefunded.compareTo(payment.getAmount()) == 0
                ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED);
        return toResponse(paymentRepository.saveAndFlush(payment));
    }

    public void delete(UUID id) {
        if (!paymentRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Payment", id);
        }
        paymentRepository.deleteById(id);
    }

    private void assertIdempotencyKeyAvailable(String idempotencyKey, UUID currentId) {
        paymentRepository.findByIdempotencyKey(idempotencyKey).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw DuplicateResourceException.of("Payment", "idempotencyKey", idempotencyKey);
            }
        });
    }

    private Enrollment resolveEnrollment(UUID enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Enrollment", enrollmentId));
    }

    private Payment getOrThrow(UUID id) {
        return paymentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getEnrollment().getId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getExternalTxnId(),
                payment.getIdempotencyKey(),
                payment.getInstallments(),
                payment.getRefundAmount(),
                payment.getRefundReason(),
                payment.getInitiatedAt(),
                payment.getCapturedAt(),
                payment.getFailureReason()
        );
    }
}
