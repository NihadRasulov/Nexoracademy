package az.demo.NexoraAcademy.entity.enrollment;


import az.demo.NexoraAcademy.entity.auth.User;
import az.demo.NexoraAcademy.entity.common.BaseEntity;
import az.demo.NexoraAcademy.entity.course.Course;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "waitlist_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WaitlistEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "status")
    private String status;
}