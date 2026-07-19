package az.demo.NexoraAcademy.entity.cohort;

import az.demo.NexoraAcademy.entity.common.BaseEntity;
import az.demo.NexoraAcademy.entity.course.Course;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "cohorts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cohort extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "seats_left", nullable = false)
    private int seatsLeft;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;
}