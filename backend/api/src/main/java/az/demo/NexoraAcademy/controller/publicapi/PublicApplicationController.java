package az.demo.NexoraAcademy.controller.publicapi;

import az.demo.NexoraAcademy.dto.platform.ApplicationRequest;
import az.demo.NexoraAcademy.service.platform.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/public/applications")
@RequiredArgsConstructor
public class PublicApplicationController {

    private final ApplicationService applicationService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> create(
            @Valid @RequestPart("data") ApplicationRequest request,
            @RequestPart("cv") MultipartFile cv) {
        applicationService.create(request, cv);
        return ResponseEntity.accepted().build();
    }
}
