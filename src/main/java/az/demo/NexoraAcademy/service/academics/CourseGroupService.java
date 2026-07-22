package az.demo.NexoraAcademy.service.academics;

import az.demo.NexoraAcademy.dto.academics.CourseGroupRequest;
import az.demo.NexoraAcademy.dto.academics.CourseGroupResponse;
import az.demo.NexoraAcademy.entity.academics.CourseGroup;
import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.enums.GroupStatus;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.InvalidStateException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.academics.CourseGroupRepository;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseGroupService {

    private final CourseGroupRepository courseGroupRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<CourseGroupResponse> findAll() {
        return courseGroupRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseGroupResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public CourseGroupResponse create(CourseGroupRequest request) {
        Course course = resolveCourse(request.courseId());
        assertGroupCodeAvailable(course.getId(), request.groupCode(), null);

        CourseGroup group = new CourseGroup();
        group.setCourse(course);
        group.setGroupCode(request.groupCode());
        applyCommonFields(group, request);

        return toResponse(courseGroupRepository.saveAndFlush(group));
    }

    public CourseGroupResponse update(UUID id, CourseGroupRequest request) {
        CourseGroup group = getOrThrow(id);
        Course course = resolveCourse(request.courseId());
        assertGroupCodeAvailable(course.getId(), request.groupCode(), id);

        group.setCourse(course);
        group.setGroupCode(request.groupCode());
        applyCommonFields(group, request);

        return toResponse(courseGroupRepository.saveAndFlush(group));
    }

    public CourseGroupResponse patch(UUID id, CourseGroupRequest request) {
        CourseGroup group = getOrThrow(id);

        if (request.courseId() != null) group.setCourse(resolveCourse(request.courseId()));
        if (request.groupCode() != null) {
            assertGroupCodeAvailable(group.getCourse().getId(), request.groupCode(), id);
            group.setGroupCode(request.groupCode());
        }
        if (request.startDate() != null) group.setStartDate(request.startDate());
        if (request.endDate() != null) group.setEndDate(request.endDate());
        if (request.registrationDeadline() != null) group.setRegistrationDeadline(request.registrationDeadline());
        if (request.totalSeats() != null) {
            assertSeatsNotBelowReserved(request.totalSeats(), group.getReservedSeats());
            group.setTotalSeats(request.totalSeats());
        }
        if (request.status() != null) group.setStatus(request.status());
        if (request.schedule() != null) group.setSchedule(request.schedule());

        return toResponse(courseGroupRepository.saveAndFlush(group));
    }

    public void delete(UUID id) {
        if (!courseGroupRepository.existsById(id)) {
            throw ResourceNotFoundException.of("CourseGroup", id);
        }
        courseGroupRepository.deleteById(id);
    }

    private void applyCommonFields(CourseGroup group, CourseGroupRequest request) {
        group.setStartDate(request.startDate());
        group.setEndDate(request.endDate());
        group.setRegistrationDeadline(request.registrationDeadline());
        group.setTotalSeats(request.totalSeats());
        group.setStatus(request.status() != null ? request.status() : GroupStatus.PLANNED);
        group.setSchedule(request.schedule() != null ? request.schedule() : new ArrayList<>());
    }

    private void assertSeatsNotBelowReserved(int totalSeats, int reservedSeats) {
        if (totalSeats < reservedSeats) {
            throw new InvalidStateException("totalSeats (" + totalSeats + ") cannot be less than reservedSeats (" + reservedSeats + ")");
        }
    }

    private void assertGroupCodeAvailable(UUID courseId, String groupCode, UUID currentId) {
        courseGroupRepository.findByCourse_IdAndGroupCode(courseId, groupCode).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw DuplicateResourceException.of("CourseGroup", "courseId+groupCode", courseId + ":" + groupCode);
            }
        });
    }

    private Course resolveCourse(UUID courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
    }

    private CourseGroup getOrThrow(UUID id) {
        return courseGroupRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CourseGroup", id));
    }

    private CourseGroupResponse toResponse(CourseGroup group) {
        return new CourseGroupResponse(
                group.getId(),
                group.getCourse().getId(),
                group.getGroupCode(),
                group.getStartDate(),
                group.getEndDate(),
                group.getRegistrationDeadline(),
                group.getTotalSeats(),
                group.getReservedSeats(),
                group.getStatus(),
                group.getSchedule(),
                group.getCreatedAt()
        );
    }
}
