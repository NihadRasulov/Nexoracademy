package az.demo.NexoraAcademy.service.crm;

import az.demo.NexoraAcademy.dto.crm.LeadRequest;
import az.demo.NexoraAcademy.dto.crm.LeadResponse;
import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.crm.Lead;
import az.demo.NexoraAcademy.entity.enums.LeadStatus;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.catalog.CourseRepository;
import az.demo.NexoraAcademy.repository.crm.LeadRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadService {

    private final LeadRepository leadRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LeadResponse> findAll() {
        return leadRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LeadResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public LeadResponse create(LeadRequest request) {
        Lead lead = new Lead();
        lead.setFullName(request.fullName());
        lead.setEmail(request.email());
        lead.setPhone(request.phone());
        lead.setCourse(resolveCourse(request.courseId()));
        lead.setSource(request.source());
        lead.setStatus(LeadStatus.NEW);
        lead.setAssignedTo(resolveUser(request.assignedTo()));
        lead.setConsentVersion(request.consentVersion());
        lead.setActivityLog(new ArrayList<>());

        return toResponse(leadRepository.saveAndFlush(lead));
    }

    public LeadResponse update(UUID id, LeadRequest request) {
        Lead lead = getOrThrow(id);

        lead.setFullName(request.fullName());
        lead.setEmail(request.email());
        lead.setPhone(request.phone());
        lead.setCourse(resolveCourse(request.courseId()));
        lead.setSource(request.source());
        lead.setAssignedTo(resolveUser(request.assignedTo()));
        lead.setConsentVersion(request.consentVersion());

        return toResponse(leadRepository.saveAndFlush(lead));
    }

    public LeadResponse patch(UUID id, LeadRequest request) {
        Lead lead = getOrThrow(id);

        if (request.fullName() != null) lead.setFullName(request.fullName());
        if (request.email() != null) lead.setEmail(request.email());
        if (request.phone() != null) lead.setPhone(request.phone());
        if (request.courseId() != null) lead.setCourse(resolveCourse(request.courseId()));
        if (request.source() != null) lead.setSource(request.source());
        if (request.assignedTo() != null) lead.setAssignedTo(resolveUser(request.assignedTo()));
        if (request.consentVersion() != null) lead.setConsentVersion(request.consentVersion());

        return toResponse(leadRepository.saveAndFlush(lead));
    }

    /** Transitions a lead's pipeline status (new -> contacted -> qualified -> converted/lost/disqualified). */
    public LeadResponse changeStatus(UUID id, LeadStatus status) {
        Lead lead = getOrThrow(id);
        lead.setStatus(status);
        return toResponse(leadRepository.saveAndFlush(lead));
    }

    public void delete(UUID id) {
        if (!leadRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Lead", id);
        }
        leadRepository.deleteById(id);
    }

    private Course resolveCourse(UUID courseId) {
        if (courseId == null) {
            return null;
        }
        return courseRepository.findById(courseId).orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Lead getOrThrow(UUID id) {
        return leadRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Lead", id));
    }

    private LeadResponse toResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getFullName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getCourse() != null ? lead.getCourse().getId() : null,
                lead.getSource(),
                lead.getStatus(),
                lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null,
                lead.getConsentVersion(),
                lead.getConsentGivenAt(),
                lead.getDuplicateOfLead() != null ? lead.getDuplicateOfLead().getId() : null,
                lead.getActivityLog(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }
}
