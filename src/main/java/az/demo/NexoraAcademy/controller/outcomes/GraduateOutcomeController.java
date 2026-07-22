package az.demo.NexoraAcademy.controller.outcomes;

import az.demo.NexoraAcademy.dto.outcomes.GraduateOutcomeRequest;
import az.demo.NexoraAcademy.dto.outcomes.GraduateOutcomeResponse;
import az.demo.NexoraAcademy.service.outcomes.GraduateOutcomeService;
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
@RequestMapping("/api/v1/graduate-outcomes")
@RequiredArgsConstructor
@Tag(name = "Graduate Outcomes")
public class GraduateOutcomeController {

    private final GraduateOutcomeService graduateOutcomeService;

    @PostMapping
    public ResponseEntity<GraduateOutcomeResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody GraduateOutcomeRequest request) {
        GraduateOutcomeResponse response = graduateOutcomeService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GraduateOutcomeResponse>> findAll() {
        return ResponseEntity.ok(graduateOutcomeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GraduateOutcomeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(graduateOutcomeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GraduateOutcomeResponse> update(@PathVariable Long id,
                                                            @Validated(ValidationGroups.OnCreate.class) @RequestBody GraduateOutcomeRequest request) {
        return ResponseEntity.ok(graduateOutcomeService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GraduateOutcomeResponse> patch(@PathVariable Long id,
                                                           @Valid @RequestBody GraduateOutcomeRequest request) {
        return ResponseEntity.ok(graduateOutcomeService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        graduateOutcomeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
