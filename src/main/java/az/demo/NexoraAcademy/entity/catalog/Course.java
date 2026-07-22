package az.demo.NexoraAcademy.entity.catalog;

import az.demo.NexoraAcademy.entity.enums.DeliveryFormat;
import az.demo.NexoraAcademy.entity.enums.DifficultyLevel;
import az.demo.NexoraAcademy.entity.identity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "courses", schema = "catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "short_description", length = 400)
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Column(nullable = false)
    private DifficultyLevel difficulty;

    @Column(name = "duration_weeks")
    private Short durationWeeks;

    @Column(name = "delivery_format", nullable = false)
    private DeliveryFormat deliveryFormat;

    @Column(name = "location_text")
    private String locationText;

    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3, columnDefinition = "bpchar(3)")
    private String currency = "AZN";

    @Column(name = "price_period", length = 30)
    private String pricePeriod;

    @Column(name = "is_published", nullable = false)
    private Boolean published = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_archived", nullable = false)
    private Boolean archived = false;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> content = new HashMap<>();
    // { objectives[], prerequisites[], tools[], media[], tags[] }

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "related_course_ids", columnDefinition = "uuid[]")
    private UUID[] relatedCourseIds = new UUID[0];

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
