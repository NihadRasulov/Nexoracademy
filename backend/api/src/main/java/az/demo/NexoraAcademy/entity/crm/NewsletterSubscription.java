package az.demo.NexoraAcademy.entity.crm;

import az.demo.NexoraAcademy.entity.support.CitextJdbcType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "newsletter_subscriptions", schema = "crm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JdbcType(CitextJdbcType.class)
    @Column(nullable = false, unique = true, columnDefinition = "citext")
    private String email;

    @Column(name = "consent_version", nullable = false, length = 20)
    private String consentVersion;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "subscribed_at", nullable = false, updatable = false)
    private Instant subscribedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
