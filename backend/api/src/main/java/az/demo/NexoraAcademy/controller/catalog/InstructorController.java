package az.demo.NexoraAcademy.controller.catalog;

import az.demo.NexoraAcademy.dto.catalog.InstructorRequest;
import az.demo.NexoraAcademy.dto.catalog.InstructorResponse;
import az.demo.NexoraAcademy.service.catalog.InstructorService;
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
@RequestMapping("/api/v1/instructors")
@RequiredArgsConstructor
@Tag(name = "Instructors")
public class InstructorController {

    private final InstructorService instructorService;

    @PostMapping
    public ResponseEntity<InstructorResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody InstructorRequest request) {
        InstructorResponse response = instructorService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InstructorResponse>> findAll() {
        return ResponseEntity.ok(instructorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(instructorService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstructorResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody InstructorRequest request) {
        return ResponseEntity.ok(instructorService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InstructorResponse> patch(@PathVariable UUID id, @Valid @RequestBody InstructorRequest request) {
        return ResponseEntity.ok(instructorService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        instructorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
