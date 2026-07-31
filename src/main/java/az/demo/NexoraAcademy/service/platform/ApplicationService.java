package az.demo.NexoraAcademy.service.platform;

import az.demo.NexoraAcademy.dto.platform.ApplicationRequest;
import az.demo.NexoraAcademy.dto.platform.ApplicationResponse;
import az.demo.NexoraAcademy.entity.platform.Application;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.platform.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applicationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public ApplicationResponse create(ApplicationRequest request) {
        Application app = new Application();
        app.setApplicationType(request.applicationType());
        app.setFullname(request.fullname());
        app.setEmail(request.email());
        app.setPhone(request.phone());
        app.setLetter(request.letter());
        return toResponse(applicationRepository.saveAndFlush(app));
    }

    public void updateCv(Long id, String filename, String cvPath) {
        Application app = getOrThrow(id);
        app.setCvFilename(filename);
        app.setCvPath(cvPath);
        applicationRepository.saveAndFlush(app);
    }

    public void delete(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Application", id);
        }
        applicationRepository.deleteById(id);
    }

    private Application getOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Application", id));
    }

    private ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getApplicationType(),
                app.getFullname(),
                app.getEmail(),
                app.getPhone(),
                app.getLetter(),
                app.getCvFilename(),
                app.getStatus(),
                app.getCreatedAt()
        );
    }
}
