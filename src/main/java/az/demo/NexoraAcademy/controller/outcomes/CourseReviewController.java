package az.demo.NexoraAcademy.controller.outcomes;

import az.demo.NexoraAcademy.dto.outcomes.CourseReviewRequest;
import az.demo.NexoraAcademy.dto.outcomes.CourseReviewResponse;
import az.demo.NexoraAcademy.service.outcomes.CourseReviewService;
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
@RequestMapping("/api/v1/course-reviews")
@RequiredArgsConstructor
@Tag(name = "Course Reviews")
public class CourseReviewController {

    private final CourseReviewService courseReviewService;

    @PostMapping
    public ResponseEntity<CourseReviewResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody CourseReviewRequest request) {
        CourseReviewResponse response = courseReviewService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CourseReviewResponse>> findAll() {
        return ResponseEntity.ok(courseReviewService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseReviewResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(courseReviewService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseReviewResponse> update(@PathVariable Long id, @Validated(ValidationGroups.OnCreate.class) @RequestBody CourseReviewRequest request) {
        return ResponseEntity.ok(courseReviewService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CourseReviewResponse> patch(@PathVariable Long id, @Valid @RequestBody CourseReviewRequest request) {
        return ResponseEntity.ok(courseReviewService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseReviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
