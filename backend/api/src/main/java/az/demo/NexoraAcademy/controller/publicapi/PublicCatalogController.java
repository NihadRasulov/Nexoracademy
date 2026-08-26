package az.demo.NexoraAcademy.controller.publicapi;

import az.demo.NexoraAcademy.dto.catalog.CategoryResponse;
import az.demo.NexoraAcademy.dto.catalog.CourseResponse;
import az.demo.NexoraAcademy.entity.enums.DeliveryFormat;
import az.demo.NexoraAcademy.entity.enums.DifficultyLevel;
import az.demo.NexoraAcademy.service.catalog.CategoryService;
import az.demo.NexoraAcademy.service.catalog.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/catalog")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CourseService courseService;
    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> categories() {
        return ResponseEntity.ok(categoryService.findPublicAll());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> category(@PathVariable Short id) {
        return ResponseEntity.ok(categoryService.findPublicById(id));
    }

    @GetMapping("/courses")
    public ResponseEntity<Page<CourseResponse>> courses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Short categoryId,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) DeliveryFormat deliveryFormat,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(courseService.searchPublished(
                q, categoryId, difficulty, deliveryFormat, pageable));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseResponse> course(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.findPublishedById(id));
    }
}
