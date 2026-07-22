package az.demo.NexoraAcademy.service.platform;

import az.demo.NexoraAcademy.dto.platform.AuditLogRequest;
import az.demo.NexoraAcademy.event.CourseCreatedEvent;
import az.demo.NexoraAcademy.event.CourseDeletedEvent;
import az.demo.NexoraAcademy.event.PaymentCompletedEvent;
import az.demo.NexoraAcademy.event.UserLoggedInEvent;
import az.demo.NexoraAcademy.event.UserRoleChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Turns domain events into AuditLog rows — decoupled from the services that
 * raise them, so business logic never has to remember to "also write an
 * audit entry." Listens AFTER_COMMIT: an action that got rolled back never
 * happened, so it shouldn't show up in the audit trail either.
 *
 * REQUIRES_NEW is deliberate — see NotificationEventListener for why a plain
 * REQUIRED propagation silently no-ops here instead of throwing.
 */
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogService auditLogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserLoggedIn(UserLoggedInEvent event) {
        auditLogService.create(new AuditLogRequest(
                event.userId(), "user.login", "User", event.userId().toString(),
                null, null, event.ipAddress()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCourseCreated(CourseCreatedEvent event) {
        auditLogService.create(new AuditLogRequest(
                event.createdBy(), "course.create", "Course", event.courseId().toString(),
                null, Map.of("title", event.title()), null));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCourseDeleted(CourseDeletedEvent event) {
        auditLogService.create(new AuditLogRequest(
                event.deletedBy(), "course.delete", "Course", event.courseId().toString(),
                Map.of("title", event.title()), null, null));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        auditLogService.create(new AuditLogRequest(
                event.userId(), "payment.complete", "Payment", event.paymentId().toString(),
                null, Map.of("amount", event.amount().toString(), "currency", event.currency()), null));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserRoleChanged(UserRoleChangedEvent event) {
        auditLogService.create(new AuditLogRequest(
                event.changedBy(), "user.role_change", "User", event.userId().toString(),
                Map.of("role", event.oldRole().name()), Map.of("role", event.newRole().name()), null));
    }
}
