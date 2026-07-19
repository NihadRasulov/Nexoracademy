package az.demo.NexoraAcademy.entity.role;

import az.demo.NexoraAcademy.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;
}