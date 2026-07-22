package az.demo.NexoraAcademy.repository.catalog;

import az.demo.NexoraAcademy.entity.catalog.CourseInstructor;
import az.demo.NexoraAcademy.entity.catalog.CourseInstructorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, CourseInstructorId> {

    List<CourseInstructor> findByCourse_Id(UUID courseId);

    List<CourseInstructor> findByInstructor_Id(UUID instructorId);
}
