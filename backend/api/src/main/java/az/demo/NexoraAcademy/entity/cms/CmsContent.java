package az.demo.NexoraAcademy.entity.cms;

import az.demo.NexoraAcademy.entity.enums.CmsContentType;
import az.demo.NexoraAcademy.entity.identity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "cms_content", schema = "cms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CmsContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String key;

    @Column(nullable = false)
    private CmsContentType type;

    @Column(length = 250)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data = new HashMap<>();
    // page: cover_image_url, author_id | faq: sort_order | social_link: platform, url | banner: cta_url, priority

    @Column(name = "is_published", nullable = false)
    private Boolean published = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
