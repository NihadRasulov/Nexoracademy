package az.demo.NexoraAcademy.entity.crm;

import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.enums.LeadSource;
import az.demo.NexoraAcademy.entity.enums.LeadStatus;
import az.demo.NexoraAcademy.entity.identity.User;
import az.demo.NexoraAcademy.entity.support.CitextJdbcType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "leads", schema = "crm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @JdbcType(CitextJdbcType.class)
    @Column(columnDefinition = "citext")
    private String email;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private LeadSource source;

    @Column(nullable = false)
    private LeadStatus status = LeadStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "consent_text_version", length = 20)
    private String consentVersion;

    @Column(name = "consent_given_at")
    private Instant consentGivenAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_lead_id")
    private Lead duplicateOfLead;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> activityLog = new ArrayList<>();
    // [{ actor_id, type, notes, created_at }]

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
