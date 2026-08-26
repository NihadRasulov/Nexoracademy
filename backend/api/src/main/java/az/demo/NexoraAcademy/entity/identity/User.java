package az.demo.NexoraAcademy.entity.identity;

import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "identity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @JdbcType(CitextJdbcType.class)
    @Column(nullable = false, unique = true, columnDefinition = "citext")
    private String email;

    @Column(length = 20)
    private String phone;

    // length 40 — bax az.demo.NexoraAcademy.validation.PersonName (DTO validasiyası ilə eyni hədd).
    @Column(name = "first_name", nullable = false, length = 40)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 40)
    private String lastName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    private UserRole role = UserRole.ADMIN;

    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(length = 10)
    private String locale = "az-AZ";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> profile = new HashMap<>();
    // { date_of_birth, address, education_bg, theme, font_scale }

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** Ad və soyadın birləşmiş forması — yalnız göstərmək üçün, bazada sütunu yoxdur. */
    @Transient
    public String getDisplayName() {
        return (firstName + " " + lastName).trim();
    }
}
