package az.demo.NexoraAcademy.entity.enrollment;


import az.demo.NexoraAcademy.entity.auth.User;
import az.demo.NexoraAcademy.entity.cohort.Cohort;
import az.demo.NexoraAcademy.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enrollments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Enrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @Column(nullable = false)
    private String status;
}