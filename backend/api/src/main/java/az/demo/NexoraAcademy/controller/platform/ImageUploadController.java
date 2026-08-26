package az.demo.NexoraAcademy.controller.platform;

import az.demo.NexoraAcademy.service.platform.ImageUploadService;
import az.demo.NexoraAcademy.service.platform.MediaUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
@Tag(name = "Uploads")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;
    private final MediaUploadService mediaUploadService;

    /**
     * Kurs cover şəkli yükləyir. Cavab: { "url": "/uploads/courses/..." }
     * Admin UI bunu course.imageUrl kimi JSON body-də göndərir.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        String url = imageUploadService.upload(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /** CMS hero bölməsi üçün yoxlanılmış MP4/WebM video yükləyir. */
    @PostMapping(path = "/media", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> uploadMedia(
            @RequestParam("file") MultipartFile file) {
        String url = mediaUploadService.upload(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
