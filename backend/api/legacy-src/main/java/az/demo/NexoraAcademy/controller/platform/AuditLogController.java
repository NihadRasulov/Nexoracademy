package az.demo.NexoraAcademy.controller.platform;

import az.demo.NexoraAcademy.dto.platform.AuditLogRequest;
import az.demo.NexoraAcademy.dto.platform.AuditLogResponse;
import az.demo.NexoraAcademy.service.platform.AuditLogService;
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
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<AuditLogResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody AuditLogRequest request) {
        AuditLogResponse response = auditLogService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> findAll() {
        return ResponseEntity.ok(auditLogService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuditLogResponse> update(@PathVariable Long id, @Validated(ValidationGroups.OnCreate.class) @RequestBody AuditLogRequest request) {
        return ResponseEntity.ok(auditLogService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AuditLogResponse> patch(@PathVariable Long id, @Valid @RequestBody AuditLogRequest request) {
        return ResponseEntity.ok(auditLogService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        auditLogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
