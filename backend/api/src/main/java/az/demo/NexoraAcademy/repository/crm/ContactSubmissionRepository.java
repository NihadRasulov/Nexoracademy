package az.demo.NexoraAcademy.repository.crm;

import az.demo.NexoraAcademy.entity.crm.ContactSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface ContactSubmissionRepository extends JpaRepository<ContactSubmission, UUID> {
    long countByStatus(String status);

    List<ContactSubmission> findAllByOrderBySubmittedAtDesc();

    List<ContactSubmission> findTop5ByOrderBySubmittedAtDesc();
}
