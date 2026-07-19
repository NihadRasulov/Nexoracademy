package az.demo.NexoraAcademy.entity.role;


import az.demo.NexoraAcademy.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name; // CHECK constraint ilə məhdudlaşdırılıb: STUDENT, ADMIN və s.[cite: 5]

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}