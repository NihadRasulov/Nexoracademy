package az.demo.NexoraAcademy.controller.academics;

import az.demo.NexoraAcademy.dto.academics.CancelEnrollmentRequest;
import az.demo.NexoraAcademy.dto.academics.EnrollmentRequest;
import az.demo.NexoraAcademy.dto.academics.EnrollmentResponse;
import az.demo.NexoraAcademy.service.academics.EnrollmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<EnrollmentResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> findAll() {
        return ResponseEntity.ok(enrollmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> patch(@PathVariable UUID id, @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EnrollmentResponse> cancel(@PathVariable UUID id, @RequestBody(required = false) CancelEnrollmentRequest request) {
        String reason = request != null ? request.reason() : null;
        return ResponseEntity.ok(enrollmentService.cancel(id, reason));
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
