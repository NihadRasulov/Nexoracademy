package az.demo.NexoraAcademy.repository.crm;

import az.demo.NexoraAcademy.entity.crm.Lead;
import az.demo.NexoraAcademy.entity.enums.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findByStatus(LeadStatus status);

    List<Lead> findByAssignedTo_Id(UUID userId);
}
