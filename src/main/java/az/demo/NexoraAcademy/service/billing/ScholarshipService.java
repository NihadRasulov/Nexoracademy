package az.demo.NexoraAcademy.service.billing;

import az.demo.NexoraAcademy.dto.billing.ScholarshipRequest;
import az.demo.NexoraAcademy.dto.billing.ScholarshipResponse;
import az.demo.NexoraAcademy.entity.billing.Scholarship;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.billing.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScholarshipService {

    private final ScholarshipRepository scholarshipRepository;

    @Transactional(readOnly = true)
    public List<ScholarshipResponse> findAll() {
        return scholarshipRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ScholarshipResponse findById(Short id) {
        return toResponse(getOrThrow(id));
    }

    public ScholarshipResponse create(ScholarshipRequest request) {
        Scholarship scholarship = new Scholarship();
        applyFields(scholarship, request);
        return toResponse(scholarshipRepository.saveAndFlush(scholarship));
    }

    public ScholarshipResponse update(Short id, ScholarshipRequest request) {
        Scholarship scholarship = getOrThrow(id);
        applyFields(scholarship, request);
        return toResponse(scholarshipRepository.saveAndFlush(scholarship));
    }

    public ScholarshipResponse patch(Short id, ScholarshipRequest request) {
        Scholarship scholarship = getOrThrow(id);

        if (request.name() != null) scholarship.setName(request.name());
        if (request.description() != null) scholarship.setDescription(request.description());
        if (request.discountPct() != null) scholarship.setDiscountPct(request.discountPct());
        if (request.maxRecipients() != null) scholarship.setMaxRecipients(request.maxRecipients());
        if (request.validFrom() != null) scholarship.setValidFrom(request.validFrom());
        if (request.validUntil() != null) scholarship.setValidUntil(request.validUntil());
        if (request.active() != null) scholarship.setActive(request.active());

        return toResponse(scholarshipRepository.saveAndFlush(scholarship));
    }

    public void delete(Short id) {
        if (!scholarshipRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Scholarship", id);
        }
        scholarshipRepository.deleteById(id);
    }

    private void applyFields(Scholarship scholarship, ScholarshipRequest request) {
        scholarship.setName(request.name());
        scholarship.setDescription(request.description());
        scholarship.setDiscountPct(request.discountPct());
        scholarship.setMaxRecipients(request.maxRecipients());
        scholarship.setValidFrom(request.validFrom());
        scholarship.setValidUntil(request.validUntil());
        scholarship.setActive(request.active() != null ? request.active() : true);
    }

    private Scholarship getOrThrow(Short id) {
        return scholarshipRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Scholarship", id));
    }

    private ScholarshipResponse toResponse(Scholarship scholarship) {
        return new ScholarshipResponse(
                scholarship.getId(),
                scholarship.getName(),
                scholarship.getDescription(),
                scholarship.getDiscountPct(),
                scholarship.getMaxRecipients(),
                scholarship.getValidFrom(),
                scholarship.getValidUntil(),
                scholarship.getActive(),
                scholarship.getApplications()
        );
    }
}
