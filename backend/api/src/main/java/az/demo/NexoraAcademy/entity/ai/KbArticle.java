package az.demo.NexoraAcademy.entity.ai;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kb_articles", schema = "ai")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KbArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "source_ref_id", columnDefinition = "TEXT")
    private String sourceRefId;

    @Column(length = 250)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // "embedding" (pgvector VECTOR(1536)) is intentionally not JPA-mapped —
    // no pgvector-java type mapper is on the classpath; it's written/read via
    // native SQL from the embedding/search service instead.

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
