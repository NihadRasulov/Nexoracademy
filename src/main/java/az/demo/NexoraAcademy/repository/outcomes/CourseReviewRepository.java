package az.demo.NexoraAcademy.repository.outcomes;

import az.demo.NexoraAcademy.entity.outcomes.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    List<CourseReview> findByCourse_Id(UUID courseId);
}
