package az.demo.NexoraAcademy.service.outcomes;

import az.demo.NexoraAcademy.dto.outcomes.GraduateOutcomeRequest;
import az.demo.NexoraAcademy.dto.outcomes.GraduateOutcomeResponse;
import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.entity.outcomes.GraduateOutcome;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import az.demo.NexoraAcademy.repository.outcomes.GraduateOutcomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GraduateOutcomeService {

    private final GraduateOutcomeRepository graduateOutcomeRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<GraduateOutcomeResponse> findAll() {
        return graduateOutcomeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public GraduateOutcomeResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public GraduateOutcomeResponse create(GraduateOutcomeRequest request) {
        GraduateOutcome outcome = new GraduateOutcome();
        outcome.setUser(resolveUser(request.userId()));
        outcome.setCourse(resolveCourse(request.courseId()));
        applyFields(outcome, request);

        return toResponse(graduateOutcomeRepository.saveAndFlush(outcome));
    }

    public GraduateOutcomeResponse update(Long id, GraduateOutcomeRequest request) {
        GraduateOutcome outcome = getOrThrow(id);
        outcome.setUser(resolveUser(request.userId()));
        outcome.setCourse(resolveCourse(request.courseId()));
        applyFields(outcome, request);

        return toResponse(graduateOutcomeRepository.saveAndFlush(outcome));
    }

    public GraduateOutcomeResponse patch(Long id, GraduateOutcomeRequest request) {
        GraduateOutcome outcome = getOrThrow(id);

        if (request.userId() != null) outcome.setUser(resolveUser(request.userId()));
        if (request.courseId() != null) outcome.setCourse(resolveCourse(request.courseId()));
        if (request.companyName() != null) outcome.setCompanyName(request.companyName());
        if (request.jobTitle() != null) outcome.setJobTitle(request.jobTitle());
        if (request.employedAt() != null) outcome.setEmployedAt(request.employedAt());
        if (request.salaryBand() != null) outcome.setSalaryBand(request.salaryBand());
        if (request.publicStory() != null) outcome.setPublicStory(request.publicStory());
        if (request.storyText() != null) outcome.setStoryText(request.storyText());

        return toResponse(graduateOutcomeRepository.saveAndFlush(outcome));
    }

    public void delete(Long id) {
        if (!graduateOutcomeRepository.existsById(id)) {
            throw ResourceNotFoundException.of("GraduateOutcome", id);
        }
        graduateOutcomeRepository.deleteById(id);
    }

    private void applyFields(GraduateOutcome outcome, GraduateOutcomeRequest request) {
        outcome.setCompanyName(request.companyName());
        outcome.setJobTitle(request.jobTitle());
        outcome.setEmployedAt(request.employedAt());
        outcome.setSalaryBand(request.salaryBand());
        outcome.setPublicStory(request.publicStory() != null ? request.publicStory() : false);
        outcome.setStoryText(request.storyText());
    }

    private User resolveUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Course resolveCourse(UUID courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
    }

    private GraduateOutcome getOrThrow(Long id) {
        return graduateOutcomeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("GraduateOutcome", id));
    }

    private GraduateOutcomeResponse toResponse(GraduateOutcome outcome) {
        return new GraduateOutcomeResponse(
                outcome.getId(),
                outcome.getUser().getId(),
                outcome.getCourse().getId(),
                outcome.getCompanyName(),
                outcome.getJobTitle(),
                outcome.getEmployedAt(),
                outcome.getSalaryBand(),
                outcome.getPublicStory(),
                outcome.getStoryText(),
                outcome.getCreatedAt()
        );
    }
}
