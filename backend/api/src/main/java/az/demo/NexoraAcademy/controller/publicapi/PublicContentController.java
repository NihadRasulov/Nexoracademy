package az.demo.NexoraAcademy.controller.publicapi;

import az.demo.NexoraAcademy.dto.billing.PublicScholarshipResponse;
import az.demo.NexoraAcademy.dto.catalog.PublicInstructorResponse;
import az.demo.NexoraAcademy.dto.cms.PublicCmsContentResponse;
import az.demo.NexoraAcademy.dto.outcomes.PublicCourseReviewResponse;
import az.demo.NexoraAcademy.dto.academics.CourseGroupResponse;
import az.demo.NexoraAcademy.entity.enums.CmsContentType;
import az.demo.NexoraAcademy.service.academics.CourseGroupService;
import az.demo.NexoraAcademy.service.billing.ScholarshipService;
import az.demo.NexoraAcademy.service.catalog.InstructorService;
import az.demo.NexoraAcademy.service.cms.CmsContentService;
import az.demo.NexoraAcademy.service.outcomes.CourseReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicContentController {
    private final CmsContentService cmsContentService;
    private final ScholarshipService scholarshipService;
    private final InstructorService instructorService;
    private final CourseGroupService courseGroupService;
    private final CourseReviewService courseReviewService;

    @GetMapping("/content/{key}")
    public ResponseEntity<PublicCmsContentResponse> contentByKey(@PathVariable String key) {
        return ResponseEntity.ok(cmsContentService.findPublishedByKey(key));
    }

    @GetMapping("/content")
    public ResponseEntity<List<PublicCmsContentResponse>> contentByType(@RequestParam CmsContentType type) {
        return ResponseEntity.ok(cmsContentService.findPublishedByType(type));
    }

    @GetMapping("/scholarships")
    public ResponseEntity<List<PublicScholarshipResponse>> scholarships() {
        return ResponseEntity.ok(scholarshipService.findPublicActive());
    }

    @GetMapping("/instructors")
    public ResponseEntity<List<PublicInstructorResponse>> instructors() {
        return ResponseEntity.ok(instructorService.findPublicActive());
    }

    @GetMapping("/courses/{courseId}/groups")
    public ResponseEntity<List<CourseGroupResponse>> groups(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseGroupService.findPublicOpenByCourse(courseId));
    }

    @GetMapping("/courses/{courseId}/reviews")
    public ResponseEntity<List<PublicCourseReviewResponse>> reviews(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseReviewService.findPublishedByCourse(courseId));
    }
}
