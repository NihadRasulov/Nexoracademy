package az.demo.NexoraAcademy.service.crm;

import az.demo.NexoraAcademy.dto.crm.ContactSubmissionRequest;
import az.demo.NexoraAcademy.dto.crm.ContactSubmissionResponse;
import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.crm.ContactSubmission;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import az.demo.NexoraAcademy.repository.crm.ContactSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactSubmissionService {

    private final ContactSubmissionRepository contactSubmissionRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<ContactSubmissionResponse> findAll() {
        return contactSubmissionRepository.findAllByOrderBySubmittedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ContactSubmissionResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public ContactSubmissionResponse create(ContactSubmissionRequest request) {
        ContactSubmission submission = new ContactSubmission();
        submission.setType(request.type());
        submission.setCourse(resolveCourse(request.courseId()));
        submission.setFullName(request.fullName());
        submission.setEmail(request.email());
        submission.setPhone(request.phone());
        submission.setMessage(request.message());
        submission.setPreferredTime(request.preferredTime());
        submission.setStatus("pending");

        return toResponse(contactSubmissionRepository.save(submission));
    }

    public ContactSubmissionResponse updateStatus(UUID id, String status) {
        ContactSubmission submission = getOrThrow(id);
        submission.setStatus(status);
        return toResponse(contactSubmissionRepository.save(submission));
    }

    public void delete(UUID id) {
        if (!contactSubmissionRepository.existsById(id)) {
            throw ResourceNotFoundException.of("ContactSubmission", id);
        }
        contactSubmissionRepository.deleteById(id);
    }

    private Course resolveCourse(UUID courseId) {
        if (courseId == null) {
            return null;
        }
        return courseRepository.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
    }

    private ContactSubmission getOrThrow(UUID id) {
        return contactSubmissionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ContactSubmission", id));
    }

    private ContactSubmissionResponse toResponse(ContactSubmission submission) {
        return new ContactSubmissionResponse(
                submission.getId(),
                submission.getType(),
                submission.getCourse() != null ? submission.getCourse().getId() : null,
                submission.getFullName(),
                submission.getEmail(),
                submission.getPhone(),
                submission.getMessage(),
                submission.getPreferredTime(),
                submission.getStatus(),
                submission.getSubmittedAt()
        );
    }
}
