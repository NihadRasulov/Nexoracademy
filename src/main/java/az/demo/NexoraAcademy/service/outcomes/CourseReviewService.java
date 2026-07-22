package az.demo.NexoraAcademy.service.outcomes;

import az.demo.NexoraAcademy.dto.outcomes.CourseReviewRequest;
import az.demo.NexoraAcademy.dto.outcomes.CourseReviewResponse;
import az.demo.NexoraAcademy.entity.academics.Enrollment;
import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.entity.outcomes.CourseReview;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.academics.EnrollmentRepository;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.repository.outcomes.CourseReviewRepository;
import az.demo.NexoraAcademy.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseReviewService {

    /** Roles that may moderate/edit/delete any user's review. Everyone else may only
     *  write their own (request.userId() must equal the caller) and only edit/delete
     *  their own — previously this endpoint had no ownership check at all, so any
     *  authenticated user could rewrite or delete anyone else's review. */
    private static final String[] STAFF_ROLES = {"ADMIN", "SYSTEM_ADMIN", "CONTENT_MANAGER"};

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public List<CourseReviewResponse> findAll() {
        return courseReviewRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseReviewResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public CourseReviewResponse create(CourseReviewRequest request) {
        boolean staff = SecurityUtils.hasAnyRole(STAFF_ROLES);
        UUID callerId = SecurityUtils.currentUserId();
        if (!staff && (callerId == null || !callerId.equals(request.userId()))) {
            throw new AccessDeniedException("You can only submit a review as yourself");
        }

        CourseReview review = new CourseReview();
        review.setCourse(resolveCourse(request.courseId()));
        review.setUser(resolveUser(request.userId()));
        review.setEnrollment(resolveEnrollment(request.enrollmentId()));
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setPublished(false);

        return toResponse(courseReviewRepository.saveAndFlush(review));
    }

    public CourseReviewResponse update(Long id, CourseReviewRequest request) {
        CourseReview review = getOrThrow(id);
        assertOwnerOrStaff(review);

        review.setCourse(resolveCourse(request.courseId()));
        review.setUser(resolveUser(request.userId()));
        review.setEnrollment(resolveEnrollment(request.enrollmentId()));
        review.setRating(request.rating());
        review.setComment(request.comment());

        return toResponse(courseReviewRepository.saveAndFlush(review));
    }

    public CourseReviewResponse patch(Long id, CourseReviewRequest request) {
        CourseReview review = getOrThrow(id);
        assertOwnerOrStaff(review);

        if (request.courseId() != null) review.setCourse(resolveCourse(request.courseId()));
        if (request.userId() != null) review.setUser(resolveUser(request.userId()));
        if (request.enrollmentId() != null) review.setEnrollment(resolveEnrollment(request.enrollmentId()));
        if (request.rating() != null) review.setRating(request.rating());
        if (request.comment() != null) review.setComment(request.comment());

        return toResponse(courseReviewRepository.saveAndFlush(review));
    }

    /** Moderation: publish/unpublish a review. */
    public CourseReviewResponse setPublished(Long id, boolean published, UUID moderatorId) {
        CourseReview review = getOrThrow(id);
        review.setPublished(published);
        review.setModeratedBy(resolveUser(moderatorId));
        return toResponse(courseReviewRepository.saveAndFlush(review));
    }

    public void delete(Long id) {
        CourseReview review = getOrThrow(id);
        assertOwnerOrStaff(review);
        courseReviewRepository.deleteById(id);
    }

    private void assertOwnerOrStaff(CourseReview review) {
        if (SecurityUtils.hasAnyRole(STAFF_ROLES)) {
            return;
        }
        UUID callerId = SecurityUtils.currentUserId();
        if (callerId == null || !callerId.equals(review.getUser().getId())) {
            throw new AccessDeniedException("You may only modify your own review");
        }
    }

    private Course resolveCourse(UUID courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Enrollment resolveEnrollment(UUID enrollmentId) {
        if (enrollmentId == null) {
            return null;
        }
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Enrollment", enrollmentId));
    }

    private CourseReview getOrThrow(Long id) {
        return courseReviewRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CourseReview", id));
    }

    private CourseReviewResponse toResponse(CourseReview review) {
        return new CourseReviewResponse(
                review.getId(),
                review.getCourse().getId(),
                review.getUser().getId(),
                review.getEnrollment() != null ? review.getEnrollment().getId() : null,
                review.getRating(),
                review.getComment(),
                review.getPublished(),
                review.getModeratedBy() != null ? review.getModeratedBy().getId() : null,
                review.getAiSentiment(),
                review.getCreatedAt()
        );
    }
}
