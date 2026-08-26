package az.demo.NexoraAcademy.controller.ai;

import az.demo.NexoraAcademy.dto.ai.KbArticleRequest;
import az.demo.NexoraAcademy.dto.ai.KbArticleResponse;
import az.demo.NexoraAcademy.service.ai.KbArticleService;
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
@RequestMapping("/api/v1/kb-articles")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base Articles")
public class KbArticleController {

    private final KbArticleService kbArticleService;

    @PostMapping
    public ResponseEntity<KbArticleResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody KbArticleRequest request) {
        KbArticleResponse response = kbArticleService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<KbArticleResponse>> findAll() {
        return ResponseEntity.ok(kbArticleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KbArticleResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(kbArticleService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KbArticleResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody KbArticleRequest request) {
        return ResponseEntity.ok(kbArticleService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<KbArticleResponse> patch(@PathVariable UUID id, @Valid @RequestBody KbArticleRequest request) {
        return ResponseEntity.ok(kbArticleService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        kbArticleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
