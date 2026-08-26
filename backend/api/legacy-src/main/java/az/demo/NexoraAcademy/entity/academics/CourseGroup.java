package az.demo.NexoraAcademy.entity.academics;

import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.enums.GroupStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "course_groups", schema = "academics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "group_code", nullable = false, length = 40)
    private String groupCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "registration_deadline")
    private Instant registrationDeadline;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "reserved_seats", nullable = false)
    private Integer reservedSeats = 0;

    @Column(nullable = false)
    private GroupStatus status = GroupStatus.PLANNED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> schedule = new ArrayList<>();
    // [{ day_of_week, start_time, end_time, location }]

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
