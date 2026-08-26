package az.demo.NexoraAcademy.controller.identity;

import az.demo.NexoraAcademy.dto.identity.SessionRequest;
import az.demo.NexoraAcademy.dto.identity.SessionResponse;
import az.demo.NexoraAcademy.service.identity.SessionService;
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
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody SessionRequest request) {
        SessionResponse response = sessionService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> findAll() {
        return ResponseEntity.ok(sessionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody SessionRequest request) {
        return ResponseEntity.ok(sessionService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SessionResponse> patch(@PathVariable UUID id, @Valid @RequestBody SessionRequest request) {
        return ResponseEntity.ok(sessionService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
