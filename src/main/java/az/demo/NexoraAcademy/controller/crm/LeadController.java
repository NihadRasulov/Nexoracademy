package az.demo.NexoraAcademy.controller.crm;

import az.demo.NexoraAcademy.dto.crm.LeadRequest;
import az.demo.NexoraAcademy.dto.crm.LeadResponse;
import az.demo.NexoraAcademy.service.crm.LeadService;
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
@RequestMapping("/api/v1/sales/leads")
@RequiredArgsConstructor
@Tag(name = "Leads")
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<LeadResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody LeadRequest request) {
        LeadResponse response = leadService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LeadResponse>> findAll() {
        return ResponseEntity.ok(leadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(leadService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody LeadRequest request) {
        return ResponseEntity.ok(leadService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LeadResponse> patch(@PathVariable UUID id, @Valid @RequestBody LeadRequest request) {
        return ResponseEntity.ok(leadService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        leadService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
