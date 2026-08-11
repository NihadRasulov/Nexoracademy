package az.demo.NexoraAcademy.controller.billing;

import az.demo.NexoraAcademy.dto.billing.ScholarshipRequest;
import az.demo.NexoraAcademy.dto.billing.ScholarshipResponse;
import az.demo.NexoraAcademy.service.billing.ScholarshipService;
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

@RestController
@RequestMapping("/api/v1/scholarships")
@RequiredArgsConstructor
@Tag(name = "Scholarships")
public class ScholarshipController {

    private final ScholarshipService scholarshipService;

    @PostMapping
    public ResponseEntity<ScholarshipResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody ScholarshipRequest request) {
        ScholarshipResponse response = scholarshipService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ScholarshipResponse>> findAll() {
        return ResponseEntity.ok(scholarshipService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScholarshipResponse> findById(@PathVariable Short id) {
        return ResponseEntity.ok(scholarshipService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScholarshipResponse> update(@PathVariable Short id, @Validated(ValidationGroups.OnCreate.class) @RequestBody ScholarshipRequest request) {
        return ResponseEntity.ok(scholarshipService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScholarshipResponse> patch(@PathVariable Short id, @Valid @RequestBody ScholarshipRequest request) {
        return ResponseEntity.ok(scholarshipService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Short id) {
        scholarshipService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(Short id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
