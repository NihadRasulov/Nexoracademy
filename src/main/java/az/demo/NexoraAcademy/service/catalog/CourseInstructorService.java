package az.demo.NexoraAcademy.service.catalog;

import az.demo.NexoraAcademy.dto.catalog.CourseInstructorRequest;
import az.demo.NexoraAcademy.dto.catalog.CourseInstructorResponse;
import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.catalog.CourseInstructor;
import az.demo.NexoraAcademy.entity.catalog.CourseInstructorId;
import az.demo.NexoraAcademy.entity.catalog.Instructor;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.catalog.CourseInstructorRepository;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import az.demo.NexoraAcademy.repository.catalog.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseInstructorService {

    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    @Transactional(readOnly = true)
    public List<CourseInstructorResponse> findAll() {
        return courseInstructorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseInstructorResponse findById(UUID courseId, UUID instructorId) {
        return toResponse(getOrThrow(new CourseInstructorId(courseId, instructorId)));
    }

    public CourseInstructorResponse create(CourseInstructorRequest request) {
        CourseInstructorId id = new CourseInstructorId(request.courseId(), request.instructorId());
        if (courseInstructorRepository.existsById(id)) {
            throw DuplicateResourceException.of("CourseInstructor", "courseId+instructorId",
                    request.courseId() + ":" + request.instructorId());
        }

        CourseInstructor courseInstructor = new CourseInstructor();
        courseInstructor.setCourse(resolveCourse(request.courseId()));
        courseInstructor.setInstructor(resolveInstructor(request.instructorId()));
        courseInstructor.setRole(request.role() != null ? request.role() : "lead");

        return toResponse(courseInstructorRepository.saveAndFlush(courseInstructor));
    }

    public CourseInstructorResponse update(UUID courseId, UUID instructorId, CourseInstructorRequest request) {
        CourseInstructor courseInstructor = getOrThrow(new CourseInstructorId(courseId, instructorId));
        courseInstructor.setRole(request.role() != null ? request.role() : "lead");
        return toResponse(courseInstructorRepository.saveAndFlush(courseInstructor));
    }

    public CourseInstructorResponse patch(UUID courseId, UUID instructorId, CourseInstructorRequest request) {
        CourseInstructor courseInstructor = getOrThrow(new CourseInstructorId(courseId, instructorId));
        if (request.role() != null) courseInstructor.setRole(request.role());
        return toResponse(courseInstructorRepository.saveAndFlush(courseInstructor));
    }

    public void delete(UUID courseId, UUID instructorId) {
        CourseInstructorId id = new CourseInstructorId(courseId, instructorId);
        if (!courseInstructorRepository.existsById(id)) {
            throw ResourceNotFoundException.of("CourseInstructor", courseId + ":" + instructorId);
        }
        courseInstructorRepository.deleteById(id);
    }

    private Course resolveCourse(UUID courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
    }

    private Instructor resolveInstructor(UUID instructorId) {
        return instructorRepository.findById(instructorId)
                .orElseThrow(() -> ResourceNotFoundException.of("Instructor", instructorId));
    }

    private CourseInstructor getOrThrow(CourseInstructorId id) {
        return courseInstructorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("CourseInstructor", id.getCourseId() + ":" + id.getInstructorId()));
    }

    private CourseInstructorResponse toResponse(CourseInstructor courseInstructor) {
        return new CourseInstructorResponse(
                courseInstructor.getCourse().getId(),
                courseInstructor.getInstructor().getId(),
                courseInstructor.getRole()
        );
    }
}
