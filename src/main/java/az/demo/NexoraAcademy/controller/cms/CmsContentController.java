package az.demo.NexoraAcademy.controller.cms;

import az.demo.NexoraAcademy.dto.cms.CmsContentRequest;
import az.demo.NexoraAcademy.dto.cms.CmsContentResponse;
import az.demo.NexoraAcademy.service.cms.CmsContentService;
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
@RequestMapping("/api/v1/content/cms-content")
@RequiredArgsConstructor
@Tag(name = "CMS Content")
public class CmsContentController {

    private final CmsContentService cmsContentService;

    @PostMapping
    public ResponseEntity<CmsContentResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody CmsContentRequest request) {
        CmsContentResponse response = cmsContentService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CmsContentResponse>> findAll() {
        return ResponseEntity.ok(cmsContentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CmsContentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(cmsContentService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CmsContentResponse> update(@PathVariable Long id, @Validated(ValidationGroups.OnCreate.class) @RequestBody CmsContentRequest request) {
        return ResponseEntity.ok(cmsContentService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CmsContentResponse> patch(@PathVariable Long id, @Valid @RequestBody CmsContentRequest request) {
        return ResponseEntity.ok(cmsContentService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cmsContentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
