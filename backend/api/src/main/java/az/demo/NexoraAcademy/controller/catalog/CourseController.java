package az.demo.NexoraAcademy.controller.catalog;

import az.demo.NexoraAcademy.dto.catalog.CourseRequest;
import az.demo.NexoraAcademy.dto.catalog.CourseResponse;
import az.demo.NexoraAcademy.entity.enums.DeliveryFormat;
import az.demo.NexoraAcademy.entity.enums.DifficultyLevel;
import az.demo.NexoraAcademy.service.catalog.CourseService;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody CourseRequest request) {
        CourseResponse response = courseService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    /** Supports free-text search (title/slug/short description) plus filters, paginated and sortable. */
    @GetMapping
    public ResponseEntity<Page<CourseResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Short categoryId,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) DeliveryFormat deliveryFormat,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(courseService.search(q, categoryId, difficulty, deliveryFormat, published, active, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CourseResponse> patch(@PathVariable UUID id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
