package az.demo.NexoraAcademy.entity.auth;


import az.demo.NexoraAcademy.entity.common.BaseEntity;
import az.demo.NexoraAcademy.entity.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import java.util.Set;

@Entity
@Table(name = "users")
@SoftDelete(columnName = "deleted_at")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
