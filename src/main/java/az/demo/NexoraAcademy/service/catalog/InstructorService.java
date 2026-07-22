package az.demo.NexoraAcademy.service.catalog;

import az.demo.NexoraAcademy.dto.catalog.InstructorRequest;
import az.demo.NexoraAcademy.dto.catalog.InstructorResponse;
import az.demo.NexoraAcademy.entity.catalog.Instructor;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.catalog.InstructorRepository;
import az.demo.NexoraAcademy.repository.identity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<InstructorResponse> findAll() {
        return instructorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InstructorResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public InstructorResponse create(InstructorRequest request) {
        Instructor instructor = new Instructor();
        instructor.setUser(resolveUser(request.userId()));
        instructor.setFullName(request.fullName());
        instructor.setBio(request.bio());
        instructor.setPhotoUrl(request.photoUrl());
        instructor.setLinkedinUrl(request.linkedinUrl());
        instructor.setCertifications(request.certifications() != null ? request.certifications() : new ArrayList<>());
        instructor.setActive(request.active() != null ? request.active() : true);
        instructor.setAvgRating(BigDecimal.ZERO);

        return toResponse(instructorRepository.saveAndFlush(instructor));
    }

    public InstructorResponse update(UUID id, InstructorRequest request) {
        Instructor instructor = getOrThrow(id);

        instructor.setUser(resolveUser(request.userId()));
        instructor.setFullName(request.fullName());
        instructor.setBio(request.bio());
        instructor.setPhotoUrl(request.photoUrl());
        instructor.setLinkedinUrl(request.linkedinUrl());
        instructor.setCertifications(request.certifications() != null ? request.certifications() : new ArrayList<>());
        instructor.setActive(request.active() != null ? request.active() : instructor.getActive());

        return toResponse(instructorRepository.saveAndFlush(instructor));
    }

    public InstructorResponse patch(UUID id, InstructorRequest request) {
        Instructor instructor = getOrThrow(id);

        if (request.userId() != null) instructor.setUser(resolveUser(request.userId()));
        if (request.fullName() != null) instructor.setFullName(request.fullName());
        if (request.bio() != null) instructor.setBio(request.bio());
        if (request.photoUrl() != null) instructor.setPhotoUrl(request.photoUrl());
        if (request.linkedinUrl() != null) instructor.setLinkedinUrl(request.linkedinUrl());
        if (request.certifications() != null) instructor.setCertifications(request.certifications());
        if (request.active() != null) instructor.setActive(request.active());

        return toResponse(instructorRepository.saveAndFlush(instructor));
    }

    public void delete(UUID id) {
        if (!instructorRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Instructor", id);
        }
        instructorRepository.deleteById(id);
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private Instructor getOrThrow(UUID id) {
        return instructorRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Instructor", id));
    }

    private InstructorResponse toResponse(Instructor instructor) {
        return new InstructorResponse(
                instructor.getId(),
                instructor.getUser() != null ? instructor.getUser().getId() : null,
                instructor.getFullName(),
                instructor.getBio(),
                instructor.getPhotoUrl(),
                instructor.getLinkedinUrl(),
                instructor.getAvgRating(),
                instructor.getCertifications(),
                instructor.getActive(),
                instructor.getCreatedAt()
        );
    }
}
