package az.demo.NexoraAcademy.entity.payment;


import az.demo.NexoraAcademy.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "refunds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Refund extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_event_id", nullable = false, unique = true)
    private PaymentEvent paymentEvent;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "reason")
    private String reason;

    @Column(name = "status")
    private String status;
}