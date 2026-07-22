package az.demo.NexoraAcademy.service.catalog;

import az.demo.NexoraAcademy.dto.catalog.CourseRequest;
import az.demo.NexoraAcademy.dto.catalog.CourseResponse;
import az.demo.NexoraAcademy.entity.catalog.Category;
import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.enums.DeliveryFormat;
import az.demo.NexoraAcademy.entity.enums.DifficultyLevel;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.event.CourseCreatedEvent;
import az.demo.NexoraAcademy.event.CourseDeletedEvent;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.catalog.CategoryRepository;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import az.demo.NexoraAcademy.repository.catalog.CourseSpecifications;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> search(String query, Short categoryId, DifficultyLevel difficulty,
                                        DeliveryFormat deliveryFormat, Boolean published, Boolean active,
                                        Pageable pageable) {
        return courseRepository
                .findAll(CourseSpecifications.search(query, categoryId, difficulty, deliveryFormat, published, active), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public CourseResponse create(CourseRequest request) {
        assertSlugAvailable(request.slug(), null);

        UUID actorId = SecurityUtils.currentUserId();

        Course course = new Course();
        course.setSlug(request.slug());
        course.setCategory(resolveCategory(request.categoryId()));
        course.setCreatedBy(actorId != null ? resolveUser(actorId) : null);
        applyCommonFields(course, request);

        Course saved = courseRepository.saveAndFlush(course);
        eventPublisher.publishEvent(new CourseCreatedEvent(saved.getId(), saved.getTitle(), actorId));

        return toResponse(saved);
    }

    public CourseResponse update(UUID id, CourseRequest request) {
        Course course = getOrThrow(id);
        assertSlugAvailable(request.slug(), id);

        course.setSlug(request.slug());
        course.setCategory(resolveCategory(request.categoryId()));
        applyCommonFields(course, request);

        return toResponse(courseRepository.saveAndFlush(course));
    }

    public CourseResponse patch(UUID id, CourseRequest request) {
        Course course = getOrThrow(id);

        if (request.slug() != null) {
            assertSlugAvailable(request.slug(), id);
            course.setSlug(request.slug());
        }
        if (request.categoryId() != null) course.setCategory(resolveCategory(request.categoryId()));
        if (request.title() != null) course.setTitle(request.title());
        if (request.shortDescription() != null) course.setShortDescription(request.shortDescription());
        if (request.fullDescription() != null) course.setFullDescription(request.fullDescription());
        if (request.targetAudience() != null) course.setTargetAudience(request.targetAudience());
        if (request.difficulty() != null) course.setDifficulty(request.difficulty());
        if (request.durationWeeks() != null) course.setDurationWeeks(request.durationWeeks());
        if (request.deliveryFormat() != null) course.setDeliveryFormat(request.deliveryFormat());
        if (request.locationText() != null) course.setLocationText(request.locationText());
        if (request.basePrice() != null) course.setBasePrice(request.basePrice());
        if (request.currency() != null) course.setCurrency(request.currency());
        if (request.pricePeriod() != null) course.setPricePeriod(request.pricePeriod());
        if (request.published() != null) course.setPublished(request.published());
        if (request.active() != null) course.setActive(request.active());
        if (request.archived() != null) course.setArchived(request.archived());
        if (request.validFrom() != null) course.setValidFrom(request.validFrom());
        if (request.validUntil() != null) course.setValidUntil(request.validUntil());
        if (request.content() != null) course.setContent(request.content());
        if (request.relatedCourseIds() != null) course.setRelatedCourseIds(request.relatedCourseIds());

        return toResponse(courseRepository.saveAndFlush(course));
    }

    public void delete(UUID id) {
        Course course = getOrThrow(id);
        courseRepository.deleteById(id);
        eventPublisher.publishEvent(new CourseDeletedEvent(id, course.getTitle(), SecurityUtils.currentUserId()));
    }

    private void applyCommonFields(Course course, CourseRequest request) {
        course.setTitle(request.title());
        course.setShortDescription(request.shortDescription());
        course.setFullDescription(request.fullDescription());
        course.setTargetAudience(request.targetAudience());
        course.setDifficulty(request.difficulty());
        course.setDurationWeeks(request.durationWeeks());
        course.setDeliveryFormat(request.deliveryFormat());
        course.setLocationText(request.locationText());
        course.setBasePrice(request.basePrice());
        course.setCurrency(request.currency() != null ? request.currency() : "AZN");
        course.setPricePeriod(request.pricePeriod());
        course.setPublished(request.published() != null ? request.published() : false);
        course.setActive(request.active() != null ? request.active() : true);
        course.setArchived(request.archived() != null ? request.archived() : false);
        course.setValidFrom(request.validFrom());
        course.setValidUntil(request.validUntil());
        course.setContent(request.content() != null ? request.content() : new HashMap<>());
        course.setRelatedCourseIds(request.relatedCourseIds() != null ? request.relatedCourseIds() : new UUID[0]);
    }

    private void assertSlugAvailable(String slug, UUID currentId) {
        courseRepository.findBySlug(slug).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw DuplicateResourceException.of("Course", "slug", slug);
            }
        });
    }

    private Category resolveCategory(Short categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
    }

    private User resolveUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Course getOrThrow(UUID id) {
        return courseRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Course", id));
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getSlug(),
                course.getCategory().getId(),
                course.getTitle(),
                course.getShortDescription(),
                course.getFullDescription(),
                course.getTargetAudience(),
                course.getDifficulty(),
                course.getDurationWeeks(),
                course.getDeliveryFormat(),
                course.getLocationText(),
                course.getBasePrice(),
                course.getCurrency(),
                course.getPricePeriod(),
                course.getPublished(),
                course.getActive(),
                course.getArchived(),
                course.getValidFrom(),
                course.getValidUntil(),
                course.getContent(),
                course.getRelatedCourseIds(),
                course.getCreatedBy() != null ? course.getCreatedBy().getId() : null,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
