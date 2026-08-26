package az.demo.NexoraAcademy.controller.academics;

import az.demo.NexoraAcademy.dto.academics.CourseGroupRequest;
import az.demo.NexoraAcademy.dto.academics.CourseGroupResponse;
import az.demo.NexoraAcademy.service.academics.CourseGroupService;
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
@RequestMapping("/api/v1/course-groups")
@RequiredArgsConstructor
@Tag(name = "Course Groups")
public class CourseGroupController {

    private final CourseGroupService courseGroupService;

    @PostMapping
    public ResponseEntity<CourseGroupResponse> create(@Validated(ValidationGroups.OnCreate.class) @RequestBody CourseGroupRequest request) {
        CourseGroupResponse response = courseGroupService.create(request);
        return ResponseEntity.created(locationOf(response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CourseGroupResponse>> findAll() {
        return ResponseEntity.ok(courseGroupService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseGroupResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(courseGroupService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseGroupResponse> update(@PathVariable UUID id, @Validated(ValidationGroups.OnCreate.class) @RequestBody CourseGroupRequest request) {
        return ResponseEntity.ok(courseGroupService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CourseGroupResponse> patch(@PathVariable UUID id, @Valid @RequestBody CourseGroupRequest request) {
        return ResponseEntity.ok(courseGroupService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        courseGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private URI locationOf(UUID id) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    }
}
