package az.demo.NexoraAcademy.controller.crm;

import az.demo.NexoraAcademy.dto.crm.ContactSubmissionRequest;
import az.demo.NexoraAcademy.dto.crm.ContactSubmissionResponse;
import az.demo.NexoraAcademy.service.crm.ContactSubmissionService;
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
@RequestMapping("/api/v1/sales/contact-submissions")
@RequiredArgsConstructor
@Tag(name = "Contact Submissions")
public class ContactSubmissionController {

    private final ContactSubmissionService contactSubmissionService;

    @PostMapping
    public ResponseEntity<ContactSubmissionResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody ContactSubmissionRequest request) {
        ContactSubmissionResponse response = contactSubmissionService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ContactSubmissionResponse>> findAll() {
        return ResponseEntity.ok(contactSubmissionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactSubmissionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(contactSubmissionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactSubmissionResponse> update(@PathVariable UUID id,
                                                              @Validated(ValidationGroups.OnCreate.class) @RequestBody ContactSubmissionRequest request) {
        return ResponseEntity.ok(contactSubmissionService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ContactSubmissionResponse> patch(@PathVariable UUID id,
                                                             @Valid @RequestBody ContactSubmissionRequest request) {
        return ResponseEntity.ok(contactSubmissionService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactSubmissionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
