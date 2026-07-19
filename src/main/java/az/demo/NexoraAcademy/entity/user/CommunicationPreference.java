package az.demo.NexoraAcademy.entity.user;


import az.demo.NexoraAcademy.entity.auth.User;
import az.demo.NexoraAcademy.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "communication_preferences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CommunicationPreference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;
}