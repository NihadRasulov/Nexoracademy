package az.demo.NexoraAcademy.service.platform;

import az.demo.NexoraAcademy.dto.platform.ApplicationRequest;
import az.demo.NexoraAcademy.dto.platform.ApplicationResponse;
import az.demo.NexoraAcademy.entity.platform.Application;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.platform.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CvStorageService cvStorageService;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public ApplicationResponse create(ApplicationRequest request, MultipartFile cv) {
        CvStorageService.StoredCv storedCv = cvStorageService.store(cv);
        Application app = new Application();
        app.setApplicationType(request.applicationType());
        app.setFullname(request.fullname());
        app.setEmail(request.email());
        app.setPhone(request.phone());
        app.setLetter(request.letter());
        app.setCvFilename(storedCv.originalName());
        app.setCvPath(storedCv.storedName());
        try {
            return toResponse(applicationRepository.save(app));
        } catch (RuntimeException exception) {
            cvStorageService.delete(storedCv.storedName());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public CvDownload downloadCv(Long id) {
        Application application = getOrThrow(id);
        if (application.getCvPath() == null || application.getCvPath().isBlank()) {
            throw new CvStorageService.CvNotFoundException();
        }
        return new CvDownload(
                application.getCvFilename(),
                cvStorageService.load(application.getCvPath()));
    }

    public ApplicationResponse updateStatus(Long id, String status) {
        Application app = getOrThrow(id);
        app.setStatus(status);
        return toResponse(applicationRepository.save(app));
    }

    public void delete(Long id) {
        Application application = getOrThrow(id);
        applicationRepository.delete(application);
        cvStorageService.delete(application.getCvPath());
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
                app.getCvPath() != null && !app.getCvPath().isBlank(),
                app.getStatus(),
                app.getCreatedAt()
        );
    }

    public record CvDownload(String filename, Resource resource) {
    }
}
