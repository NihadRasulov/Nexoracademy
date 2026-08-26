package az.demo.NexoraAcademy.controller.crm;

import az.demo.NexoraAcademy.dto.crm.ContactSubmissionResponse;
import az.demo.NexoraAcademy.dto.crm.ContactStatusRequest;
import az.demo.NexoraAcademy.service.crm.ContactSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contact-submissions")
@RequiredArgsConstructor
@Tag(name = "Contact Submissions")
public class ContactSubmissionController {

    private final ContactSubmissionService contactSubmissionService;

    @GetMapping
    public ResponseEntity<List<ContactSubmissionResponse>> findAll() {
        return ResponseEntity.ok(contactSubmissionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactSubmissionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(contactSubmissionService.findById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ContactSubmissionResponse> updateStatus(@PathVariable UUID id,
                                                                   @Valid @RequestBody ContactStatusRequest request) {
        return ResponseEntity.ok(contactSubmissionService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactSubmissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
