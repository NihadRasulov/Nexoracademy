package az.demo.NexoraAcademy.repository.academics;

import az.demo.NexoraAcademy.entity.academics.CourseGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseGroupRepository extends JpaRepository<CourseGroup, UUID> {

    List<CourseGroup> findByCourse_Id(UUID courseId);

    Optional<CourseGroup> findByCourse_IdAndGroupCode(UUID courseId, String groupCode);
}
