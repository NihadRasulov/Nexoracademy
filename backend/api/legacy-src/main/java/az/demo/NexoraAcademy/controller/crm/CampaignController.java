package az.demo.NexoraAcademy.controller.crm;

import az.demo.NexoraAcademy.dto.crm.CampaignRequest;
import az.demo.NexoraAcademy.dto.crm.CampaignResponse;
import az.demo.NexoraAcademy.service.crm.CampaignService;
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
@RequestMapping("/api/v1/sales/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody CampaignRequest request) {
        CampaignResponse response = campaignService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CampaignResponse>> findAll() {
        return ResponseEntity.ok(campaignService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(campaignService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody CampaignRequest request) {
        return ResponseEntity.ok(campaignService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CampaignResponse> patch(@PathVariable UUID id, @Valid @RequestBody CampaignRequest request) {
        return ResponseEntity.ok(campaignService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
