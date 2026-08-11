package az.demo.NexoraAcademy.entity.outcomes;

import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.identity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "graduate_outcomes", schema = "outcomes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraduateOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(name = "employed_at")
    private LocalDate employedAt;

    @Column(name = "salary_band", length = 50)
    private String salaryBand;

    @Column(name = "is_public_story", nullable = false)
    private Boolean publicStory = false;

    @Column(name = "story_text", columnDefinition = "TEXT")
    private String storyText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
