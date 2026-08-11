package az.demo.NexoraAcademy.repository.outcomes;

import az.demo.NexoraAcademy.entity.outcomes.CourseReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    @EntityGraph(attributePaths = {"course", "user", "enrollment"})
    List<CourseReview> findByCourse_Id(UUID courseId);

    @EntityGraph(attributePaths = {"course", "user"})
    List<CourseReview> findByCourse_IdAndPublishedTrueOrderByCreatedAtDesc(UUID courseId);

    @EntityGraph(attributePaths = {"course", "user"})
    List<CourseReview> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}
