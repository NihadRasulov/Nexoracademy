package az.demo.NexoraAcademy.service.academics;

import az.demo.NexoraAcademy.dto.academics.EnrollmentRequest;
import az.demo.NexoraAcademy.dto.academics.EnrollmentResponse;
import az.demo.NexoraAcademy.entity.academics.CourseGroup;
import az.demo.NexoraAcademy.entity.academics.Enrollment;
import az.demo.NexoraAcademy.entity.enums.EnrollmentStatus;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.event.EnrollmentConfirmedEvent;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.InvalidStateException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.academics.CourseGroupRepository;
import az.demo.NexoraAcademy.repository.academics.EnrollmentRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private static final Set<EnrollmentStatus> SEAT_OCCUPYING_STATUSES = Set.of(
            EnrollmentStatus.HELD, EnrollmentStatus.PENDING_PAYMENT, EnrollmentStatus.CONFIRMED, EnrollmentStatus.COMPLETED);

    /**
     * Roles that may act on any user's enrollment (list everyone's, set an arbitrary
     * status, edit/cancel/delete someone else's). Everyone else may only create their
     * own enrollment (always starting at PENDING_PAYMENT — status is not client-settable)
     * and view/cancel their own. See create()/findAll()/findById()/update()/patch()/
     * delete()/cancel() below — this closes a bug where any authenticated STUDENT could
     * self-enroll (or enroll another user) with status=CONFIRMED directly, skipping
     * payment entirely, and could list/modify every user's enrollments.
     */
    private static final String[] STAFF_ROLES = {"ADMIN", "SYSTEM_ADMIN", "SALES_CRM"};

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findAll() {
        if (!SecurityUtils.hasAnyRole(STAFF_ROLES)) {
            throw new AccessDeniedException("Only staff may list all enrollments");
        }
        return enrollmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse findById(UUID id) {
        Enrollment enrollment = getOrThrow(id);
        assertOwnerOrStaff(enrollment);
        return toResponse(enrollment);
    }

    public EnrollmentResponse create(EnrollmentRequest request) {
        boolean staff = SecurityUtils.hasAnyRole(STAFF_ROLES);
        UUID callerId = SecurityUtils.currentUserId();
        if (!staff && (callerId == null || !callerId.equals(request.userId()))) {
            throw new AccessDeniedException("You can only enroll yourself");
        }

        assertIdempotencyKeyAvailable(request.idempotencyKey(), null);
        assertUserGroupAvailable(request.userId(), request.groupId(), null);

        CourseGroup group = resolveGroup(request.groupId());
        // Non-staff can never set status directly — every self-enrollment starts at
        // PENDING_PAYMENT and only moves to CONFIRMED via the payment capture flow.
        EnrollmentStatus status = (staff && request.status() != null) ? request.status() : EnrollmentStatus.PENDING_PAYMENT;
        assertEnrollable(group, status);

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(resolveUser(request.userId()));
        enrollment.setGroup(group);
        enrollment.setStatus(status);
        enrollment.setIdempotencyKey(request.idempotencyKey());
        enrollment.setConsentVersion(request.consentVersion());
        enrollment.setConsentGivenAt(request.consentGivenAt());

        if (SEAT_OCCUPYING_STATUSES.contains(status)) {
            occupySeat(group);
        }

        Enrollment saved = enrollmentRepository.saveAndFlush(enrollment);
        if (status == EnrollmentStatus.CONFIRMED) {
            eventPublisher.publishEvent(new EnrollmentConfirmedEvent(saved.getId(), saved.getUser().getId(), group.getId()));
        }

        return toResponse(saved);
    }

    public EnrollmentResponse update(UUID id, EnrollmentRequest request) {
        assertStaff();
        Enrollment enrollment = getOrThrow(id);
        assertIdempotencyKeyAvailable(request.idempotencyKey(), id);
        assertUserGroupAvailable(request.userId(), request.groupId(), id);

        enrollment.setUser(resolveUser(request.userId()));
        enrollment.setGroup(resolveGroup(request.groupId()));
        if (request.status() != null) {
            applyStatusTransition(enrollment, request.status());
        }
        enrollment.setIdempotencyKey(request.idempotencyKey());
        enrollment.setConsentVersion(request.consentVersion());
        enrollment.setConsentGivenAt(request.consentGivenAt());

        return toResponse(enrollmentRepository.saveAndFlush(enrollment));
    }

    public EnrollmentResponse patch(UUID id, EnrollmentRequest request) {
        assertStaff();
        Enrollment enrollment = getOrThrow(id);

        if (request.userId() != null || request.groupId() != null) {
            UUID userId = request.userId() != null ? request.userId() : enrollment.getUser().getId();
            UUID groupId = request.groupId() != null ? request.groupId() : enrollment.getGroup().getId();
            assertUserGroupAvailable(userId, groupId, id);
            if (request.userId() != null) enrollment.setUser(resolveUser(request.userId()));
            if (request.groupId() != null) enrollment.setGroup(resolveGroup(request.groupId()));
        }
        if (request.status() != null) applyStatusTransition(enrollment, request.status());
        if (request.idempotencyKey() != null) {
            assertIdempotencyKeyAvailable(request.idempotencyKey(), id);
            enrollment.setIdempotencyKey(request.idempotencyKey());
        }
        if (request.consentVersion() != null) enrollment.setConsentVersion(request.consentVersion());
        if (request.consentGivenAt() != null) enrollment.setConsentGivenAt(request.consentGivenAt());

        return toResponse(enrollmentRepository.saveAndFlush(enrollment));
    }

    /** Dedicated cancel operation — releases the seat it was holding and records why. Owner or staff. */
    public EnrollmentResponse cancel(UUID id, String reason) {
        Enrollment enrollment = getOrThrow(id);
        assertOwnerOrStaff(enrollment);

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new InvalidStateException("Enrollment " + id + " is already cancelled");
        }
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new InvalidStateException("Enrollment " + id + " is already completed and cannot be cancelled");
        }

        if (SEAT_OCCUPYING_STATUSES.contains(enrollment.getStatus())) {
            releaseSeat(enrollment.getGroup());
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollment.setCancelledAt(Instant.now());
        enrollment.setCancelReason(reason);

        return toResponse(enrollmentRepository.saveAndFlush(enrollment));
    }

    public void delete(UUID id) {
        assertStaff();
        Enrollment enrollment = getOrThrow(id);
        if (SEAT_OCCUPYING_STATUSES.contains(enrollment.getStatus())) {
            releaseSeat(enrollment.getGroup());
        }
        enrollmentRepository.deleteById(id);
    }

    private void assertStaff() {
        if (!SecurityUtils.hasAnyRole(STAFF_ROLES)) {
            throw new AccessDeniedException("Only staff may perform this action");
        }
    }

    private void assertOwnerOrStaff(Enrollment enrollment) {
        if (SecurityUtils.hasAnyRole(STAFF_ROLES)) {
            return;
        }
        UUID callerId = SecurityUtils.currentUserId();
        if (callerId == null || !callerId.equals(enrollment.getUser().getId())) {
            throw new AccessDeniedException("You may only access your own enrollment");
        }
    }

    private void applyStatusTransition(Enrollment enrollment, EnrollmentStatus newStatus) {
        if (newStatus == enrollment.getStatus()) {
            return;
        }
        Instant now = Instant.now();
        boolean wasOccupying = SEAT_OCCUPYING_STATUSES.contains(enrollment.getStatus());
        boolean willOccupy = SEAT_OCCUPYING_STATUSES.contains(newStatus);

        if (wasOccupying && !willOccupy) {
            releaseSeat(enrollment.getGroup());
        } else if (!wasOccupying && willOccupy) {
            occupySeat(enrollment.getGroup());
        }

        if (newStatus == EnrollmentStatus.COMPLETED) enrollment.setCompletedAt(now);
        if (newStatus == EnrollmentStatus.CANCELLED) enrollment.setCancelledAt(now);
        enrollment.setStatus(newStatus);

        if (newStatus == EnrollmentStatus.CONFIRMED) {
            eventPublisher.publishEvent(
                    new EnrollmentConfirmedEvent(enrollment.getId(), enrollment.getUser().getId(), enrollment.getGroup().getId()));
        }
    }

    private void assertEnrollable(CourseGroup group, EnrollmentStatus status) {
        if (!SEAT_OCCUPYING_STATUSES.contains(status)) {
            return;
        }
        if (group.getRegistrationDeadline() != null && group.getRegistrationDeadline().isBefore(Instant.now())) {
            throw new InvalidStateException("Registration deadline for course group " + group.getId() + " has passed");
        }
        if (group.getReservedSeats() >= group.getTotalSeats()) {
            throw new InvalidStateException("Course group " + group.getId() + " is full");
        }
    }

    private void occupySeat(CourseGroup group) {
        if (group.getReservedSeats() >= group.getTotalSeats()) {
            throw new InvalidStateException("Course group " + group.getId() + " is full");
        }
        group.setReservedSeats(group.getReservedSeats() + 1);
        courseGroupRepository.saveAndFlush(group);
    }

    private void releaseSeat(CourseGroup group) {
        group.setReservedSeats(Math.max(0, group.getReservedSeats() - 1));
        courseGroupRepository.saveAndFlush(group);
    }

    private void assertIdempotencyKeyAvailable(String idempotencyKey, UUID currentId) {
        enrollmentRepository.findByIdempotencyKey(idempotencyKey).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw DuplicateResourceException.of("Enrollment", "idempotencyKey", idempotencyKey);
            }
        });
    }

    private void assertUserGroupAvailable(UUID userId, UUID groupId, UUID currentId) {
        enrollmentRepository.findByUser_IdAndGroup_Id(userId, groupId).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw DuplicateResourceException.of("Enrollment", "userId+groupId", userId + ":" + groupId);
            }
        });
    }

    private User resolveUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private CourseGroup resolveGroup(UUID groupId) {
        return courseGroupRepository.findById(groupId).orElseThrow(() -> ResourceNotFoundException.of("CourseGroup", groupId));
    }

    private Enrollment getOrThrow(UUID id) {
        return enrollmentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Enrollment", id));
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getUser().getId(),
                enrollment.getGroup().getId(),
                enrollment.getStatus(),
                enrollment.getIdempotencyKey(),
                enrollment.getConsentVersion(),
                enrollment.getConsentGivenAt(),
                enrollment.getHoldExpiresAt(),
                enrollment.getEnrolledAt(),
                enrollment.getCompletedAt(),
                enrollment.getCancelledAt(),
                enrollment.getCancelReason()
        );
    }
}
