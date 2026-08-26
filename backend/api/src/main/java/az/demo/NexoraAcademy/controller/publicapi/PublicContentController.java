package az.demo.NexoraAcademy.controller.publicapi;

import az.demo.NexoraAcademy.dto.cms.PublicCmsContentResponse;
import az.demo.NexoraAcademy.entity.enums.CmsContentType;
import az.demo.NexoraAcademy.service.cms.CmsContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicContentController {
    private final CmsContentService cmsContentService;

    @GetMapping("/content/{key}")
    public ResponseEntity<PublicCmsContentResponse> contentByKey(@PathVariable String key) {
        return ResponseEntity.ok(cmsContentService.findPublishedByKey(key));
    }

    @GetMapping("/content")
    public ResponseEntity<List<PublicCmsContentResponse>> contentByType(@RequestParam CmsContentType type) {
        return ResponseEntity.ok(cmsContentService.findPublishedByType(type));
    }
}
