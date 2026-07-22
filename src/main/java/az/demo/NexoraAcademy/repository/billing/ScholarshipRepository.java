package az.demo.NexoraAcademy.repository.billing;

import az.demo.NexoraAcademy.entity.billing.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Short> {
}
