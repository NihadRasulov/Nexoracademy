package az.demo.NexoraAcademy.controller.platform;

import az.demo.NexoraAcademy.dto.platform.ApplicationRequest;
import az.demo.NexoraAcademy.dto.platform.ApplicationResponse;
import az.demo.NexoraAcademy.service.platform.ApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApplicationResponse> create(
            @org.springframework.web.bind.annotation.RequestPart("data") ApplicationRequest request,
            @org.springframework.web.bind.annotation.RequestPart(value = "cv", required = false) org.springframework.web.multipart.MultipartFile cv) throws IOException {

        ApplicationResponse response = applicationService.create(request);

        if (cv != null && !cv.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + cv.getOriginalFilename();
            Path uploadDir = Paths.get("/app/uploads/cvs");
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(filename);
            cv.transferTo(filePath.toFile());
            applicationService.updateCv(response.id(), filename, "/cvs/" + filename);
            response = applicationService.findById(response.id());
        }

        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> findAll() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
