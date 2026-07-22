package az.demo.NexoraAcademy.repository.billing;

import az.demo.NexoraAcademy.entity.billing.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByEnrollment_Id(UUID enrollmentId);
}
