package az.demo.NexoraAcademy.repository.crm;

import az.demo.NexoraAcademy.entity.crm.Lead;
import az.demo.NexoraAcademy.entity.enums.LeadStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import az.demo.NexoraAcademy.entity.enums.LeadSource;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    @EntityGraph(attributePaths = {"course", "assignedTo"})
    List<Lead> findByStatus(LeadStatus status);

    @EntityGraph(attributePaths = {"course", "assignedTo"})
    List<Lead> findByAssignedTo_Id(UUID userId);

    Optional<Lead> findFirstByEmailAndSource(String email, LeadSource source);
}
