package az.demo.NexoraAcademy.service.billing;

import az.demo.NexoraAcademy.dto.billing.PaymentCallbackRequest;
import az.demo.NexoraAcademy.entity.academics.Enrollment;
import az.demo.NexoraAcademy.entity.billing.Payment;
import az.demo.NexoraAcademy.entity.enums.PaymentStatus;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.event.PaymentCompletedEvent;
import az.demo.NexoraAcademy.exception.InvalidStateException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.academics.EnrollmentRepository;
import az.demo.NexoraAcademy.repository.billing.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Enrollment enrollment = new Enrollment();
        enrollment.setId(UUID.randomUUID());
        enrollment.setUser(user);

        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setEnrollment(enrollment);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("AZN");
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setRefundAmount(BigDecimal.ZERO);
        payment.setIdempotencyKey("pay-idem-1");

        lenient().when(paymentRepository.saveAndFlush(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void captureTransitionsToCaptureAndPublishesEvent() {
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        paymentService.capture(payment.getId());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(payment.getCapturedAt()).isNotNull();
        verify(eventPublisher, times(1)).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    void captureThrowsInvalidStateExceptionWhenAlreadyCaptured() {
        payment.setStatus(PaymentStatus.CAPTURED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.capture(payment.getId()))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void refundPartialAmountLeavesStatusPartiallyRefunded() {
        payment.setStatus(PaymentStatus.CAPTURED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        var response = paymentService.refund(payment.getId(), new BigDecimal("40.00"), "customer request");

        assertThat(response.status()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
        assertThat(response.refundAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void refundFullAmountMarksStatusRefunded() {
        payment.setStatus(PaymentStatus.CAPTURED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        var response = paymentService.refund(payment.getId(), new BigDecimal("100.00"), "full refund");

        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void refundThrowsInvalidStateExceptionWhenExceedingPaidAmount() {
        payment.setStatus(PaymentStatus.CAPTURED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refund(payment.getId(), new BigDecimal("150.00"), "too much"))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("exceeds");
    }

    @Test
    void handleCallbackCapturedTransitionsPaymentByIdempotencyKey() {
        when(paymentRepository.findByIdempotencyKey("pay-idem-1")).thenReturn(Optional.of(payment));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        PaymentCallbackRequest callback = new PaymentCallbackRequest("pay-idem-1", "captured", "ext-txn-123", null);
        var response = paymentService.handleCallback(callback);

        assertThat(response.status()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(payment.getExternalTxnId()).isEqualTo("ext-txn-123");
    }

    @Test
    void handleCallbackFailedSetsFailureReason() {
        when(paymentRepository.findByIdempotencyKey("pay-idem-1")).thenReturn(Optional.of(payment));

        PaymentCallbackRequest callback = new PaymentCallbackRequest("pay-idem-1", "failed", null, "insufficient funds");
        var response = paymentService.handleCallback(callback);

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("insufficient funds");
    }

    @Test
    void handleCallbackThrowsResourceNotFoundExceptionWhenKeyUnknown() {
        when(paymentRepository.findByIdempotencyKey("unknown-key")).thenReturn(Optional.empty());

        PaymentCallbackRequest callback = new PaymentCallbackRequest("unknown-key", "captured", null, null);

        assertThatThrownBy(() -> paymentService.handleCallback(callback))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
