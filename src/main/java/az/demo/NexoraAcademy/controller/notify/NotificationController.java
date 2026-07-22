package az.demo.NexoraAcademy.controller.notify;

import az.demo.NexoraAcademy.dto.notify.NotificationRequest;
import az.demo.NexoraAcademy.dto.notify.NotificationResponse;
import az.demo.NexoraAcademy.service.notify.NotificationService;
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
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> findAll() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NotificationResponse> patch(@PathVariable UUID id, @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
