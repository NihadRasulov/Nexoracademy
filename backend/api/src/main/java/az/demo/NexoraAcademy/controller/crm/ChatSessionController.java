package az.demo.NexoraAcademy.controller.crm;

import az.demo.NexoraAcademy.dto.crm.ChatSessionRequest;
import az.demo.NexoraAcademy.dto.crm.ChatSessionResponse;
import az.demo.NexoraAcademy.service.crm.ChatSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/chat-sessions")
@RequiredArgsConstructor
@Tag(name = "Chat Sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    public ResponseEntity<ChatSessionResponse> create(@Valid @RequestBody ChatSessionRequest request) {
        ChatSessionResponse response = chatSessionService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChatSessionResponse>> findAll() {
        return ResponseEntity.ok(chatSessionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatSessionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(chatSessionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChatSessionResponse> update(@PathVariable UUID id, @Valid @RequestBody ChatSessionRequest request) {
        return ResponseEntity.ok(chatSessionService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ChatSessionResponse> patch(@PathVariable UUID id, @Valid @RequestBody ChatSessionRequest request) {
        return ResponseEntity.ok(chatSessionService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        chatSessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
