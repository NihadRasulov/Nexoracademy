package az.demo.NexoraAcademy.entity.payment;


import az.demo.NexoraAcademy.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "provider_event_id", nullable = false, unique = true)
    private String providerEventId;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String payload;
}