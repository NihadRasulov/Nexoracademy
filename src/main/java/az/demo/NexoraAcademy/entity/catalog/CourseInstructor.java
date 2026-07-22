package az.demo.NexoraAcademy.entity.catalog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course_instructors", schema = "catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseInstructor {

    @EmbeddedId
    private CourseInstructorId id = new CourseInstructorId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("instructorId")
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @Column(nullable = false, length = 40)
    private String role = "lead";
}
