package az.demo.NexoraAcademy.entity.auth;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_credentials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserCredential {

    @Id
    private java.util.UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}