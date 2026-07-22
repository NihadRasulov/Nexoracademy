package az.demo.NexoraAcademy.entity.crm;

import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.enums.SubmissionType;
import az.demo.NexoraAcademy.entity.support.CitextJdbcType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contact_submissions", schema = "crm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @Column(nullable = false)
    private SubmissionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @JdbcType(CitextJdbcType.class)
    @Column(columnDefinition = "citext")
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "preferred_time")
    private Instant preferredTime;

    @Column(length = 20)
    private String status = "pending";

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;
}
