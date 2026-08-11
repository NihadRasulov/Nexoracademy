package az.demo.NexoraAcademy.controller.catalog;

import az.demo.NexoraAcademy.dto.catalog.CourseInstructorRequest;
import az.demo.NexoraAcademy.dto.catalog.CourseInstructorResponse;
import az.demo.NexoraAcademy.service.catalog.CourseInstructorService;
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
@RequestMapping("/api/v1/course-instructors")
@RequiredArgsConstructor
@Tag(name = "Course Instructors")
public class CourseInstructorController {

    private final CourseInstructorService courseInstructorService;

    @PostMapping
    public ResponseEntity<CourseInstructorResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody CourseInstructorRequest request) {
        CourseInstructorResponse response = courseInstructorService.create(request);
        return ResponseEntity.created(locationOf(response.courseId(), response.instructorId())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CourseInstructorResponse>> findAll() {
        return ResponseEntity.ok(courseInstructorService.findAll());
    }

    @GetMapping("/{courseId}/{instructorId}")
    public ResponseEntity<CourseInstructorResponse> findById(@PathVariable UUID courseId, @PathVariable UUID instructorId) {
        return ResponseEntity.ok(courseInstructorService.findById(courseId, instructorId));
    }

    @PutMapping("/{courseId}/{instructorId}")
    public ResponseEntity<CourseInstructorResponse> update(@PathVariable UUID courseId, @PathVariable UUID instructorId,
                                                             @Validated(ValidationGroups.OnCreate.class) @RequestBody CourseInstructorRequest request) {
        return ResponseEntity.ok(courseInstructorService.update(courseId, instructorId, request));
    }

    @PatchMapping("/{courseId}/{instructorId}")
    public ResponseEntity<CourseInstructorResponse> patch(@PathVariable UUID courseId, @PathVariable UUID instructorId,
                                                            @Valid @RequestBody CourseInstructorRequest request) {
        return ResponseEntity.ok(courseInstructorService.patch(courseId, instructorId, request));
    }

    @DeleteMapping("/{courseId}/{instructorId}")
    public ResponseEntity<Void> delete(@PathVariable UUID courseId, @PathVariable UUID instructorId) {
        courseInstructorService.delete(courseId, instructorId);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID courseId, UUID instructorId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{courseId}/{instructorId}")
                .buildAndExpand(courseId, instructorId)
                .toUri();
    }
}
