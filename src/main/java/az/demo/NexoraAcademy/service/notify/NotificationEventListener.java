package az.demo.NexoraAcademy.service.notify;

import az.demo.NexoraAcademy.dto.notify.NotificationRequest;
import az.demo.NexoraAcademy.dto.notify.NotificationResponse;
import az.demo.NexoraAcademy.entity.enums.NotificationChannel;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.event.EnrollmentConfirmedEvent;
import az.demo.NexoraAcademy.event.PaymentCompletedEvent;
import az.demo.NexoraAcademy.event.UserRegisteredEvent;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Turns domain events into in-app Notification rows + a best-effort email.
 * Listens AFTER_COMMIT so a notification is never created for a transaction
 * that ends up rolling back.
 *
 * REQUIRES_NEW is deliberate: at AFTER_COMMIT time the original transaction's
 * synchronization hasn't been fully torn down yet, so a plain REQUIRED call
 * here would silently attach to that already-completing transaction instead
 * of opening a fresh one — the write would appear to succeed (no exception)
 * but never actually persist.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEnrollmentConfirmed(EnrollmentConfirmedEvent event) {
        NotificationResponse notification = notificationService.create(new NotificationRequest(
                event.userId(),
                "enrollment_confirmed",
                NotificationChannel.EMAIL,
                Map.of("enrollmentId", event.enrollmentId().toString(), "groupId", event.groupId().toString())
        ));
        notificationService.markSent(notification.id());

        findUser(event.userId()).ifPresent(user -> emailService.send(
                user.getEmail(),
                "Your enrollment is confirmed",
                "Good news! Your enrollment (" + event.enrollmentId() + ") has been confirmed. See you in class."
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        NotificationResponse notification = notificationService.create(new NotificationRequest(
                event.userId(),
                "payment_completed",
                NotificationChannel.EMAIL,
                Map.of("paymentId", event.paymentId().toString(), "amount", event.amount().toString(), "currency", event.currency())
        ));
        notificationService.markSent(notification.id());

        findUser(event.userId()).ifPresent(user -> emailService.send(
                user.getEmail(),
                "Payment received",
                "We received your payment of " + event.amount() + " " + event.currency() + ". Thank you!"
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserRegistered(UserRegisteredEvent event) {
        notificationService.create(new NotificationRequest(
                event.userId(),
                "welcome",
                NotificationChannel.IN_APP,
                Map.of("email", event.email())
        ));
    }

    private Optional<User> findUser(UUID userId) {
        return userRepository.findById(userId);
    }
}
