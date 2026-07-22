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
import az.demo.NexoraAcademy.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
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
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseGroupRepository courseGroupRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User user;
    private CourseGroup group;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        group = new CourseGroup();
        group.setId(UUID.randomUUID());
        group.setTotalSeats(2);
        group.setReservedSeats(0);

        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(courseGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        lenient().when(enrollmentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        lenient().when(enrollmentRepository.findByUser_IdAndGroup_Id(any(), any())).thenReturn(Optional.empty());
        lenient().when(enrollmentRepository.saveAndFlush(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            if (e.getId() == null) e.setId(UUID.randomUUID());
            return e;
        });

        // EnrollmentService now enforces ownership/role checks via SecurityUtils
        // (see EnrollmentService's STAFF_ROLES) — these tests exercise business
        // logic (seat capacity, events, exceptions), not authorization, so run
        // as staff to bypass ownership restrictions.
        AuthenticatedUser staff = new AuthenticatedUser(UUID.randomUUID(), "staff@example.com", "hash", true, true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(staff, null, staff.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOccupiesASeatWhenStatusIsSeatOccupying() {
        EnrollmentRequest request = new EnrollmentRequest(user.getId(), group.getId(), EnrollmentStatus.PENDING_PAYMENT,
                "idem-key-001", null, null);

        enrollmentService.create(request);

        assertThat(group.getReservedSeats()).isEqualTo(1);
        verify(courseGroupRepository).saveAndFlush(group);
    }

    @Test
    void createThrowsInvalidStateExceptionWhenGroupIsFull() {
        group.setReservedSeats(2); // already at totalSeats capacity

        EnrollmentRequest request = new EnrollmentRequest(user.getId(), group.getId(), EnrollmentStatus.PENDING_PAYMENT,
                "idem-key-002", null, null);

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("full");

        verify(enrollmentRepository, org.mockito.Mockito.never()).saveAndFlush(any());
    }

    @Test
    void createThrowsInvalidStateExceptionWhenRegistrationDeadlinePassed() {
        group.setRegistrationDeadline(Instant.now().minusSeconds(3600));

        EnrollmentRequest request = new EnrollmentRequest(user.getId(), group.getId(), EnrollmentStatus.PENDING_PAYMENT,
                "idem-key-003", null, null);

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("deadline");
    }

    @Test
    void createThrowsDuplicateResourceExceptionForRepeatedIdempotencyKey() {
        Enrollment existing = new Enrollment();
        existing.setId(UUID.randomUUID());
        when(enrollmentRepository.findByIdempotencyKey("dup-key")).thenReturn(Optional.of(existing));

        EnrollmentRequest request = new EnrollmentRequest(user.getId(), group.getId(), EnrollmentStatus.PENDING_PAYMENT,
                "dup-key", null, null);

        assertThatThrownBy(() -> enrollmentService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createPublishesEnrollmentConfirmedEventWhenStatusIsConfirmed() {
        EnrollmentRequest request = new EnrollmentRequest(user.getId(), group.getId(), EnrollmentStatus.CONFIRMED,
                "idem-key-004", null, null);

        enrollmentService.create(request);

        verify(eventPublisher, times(1)).publishEvent(any(EnrollmentConfirmedEvent.class));
    }

    @Test
    void cancelReleasesTheSeatAndSetsReason() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(UUID.randomUUID());
        enrollment.setUser(user);
        enrollment.setGroup(group);
        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        group.setReservedSeats(1);
        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));

        EnrollmentResponse response = enrollmentService.cancel(enrollment.getId(), "Changed my mind");

        assertThat(response.status()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(response.cancelReason()).isEqualTo("Changed my mind");
        assertThat(group.getReservedSeats()).isEqualTo(0);
    }

    @Test
    void cancelThrowsInvalidStateExceptionWhenAlreadyCancelled() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(UUID.randomUUID());
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.cancel(enrollment.getId(), "again"))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void findByIdThrowsResourceNotFoundExceptionWhenMissing() {
        UUID missingId = UUID.randomUUID();
        when(enrollmentRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
