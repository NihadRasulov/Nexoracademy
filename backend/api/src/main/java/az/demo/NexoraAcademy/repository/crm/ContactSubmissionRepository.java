package az.demo.NexoraAcademy.repository.crm;

import az.demo.NexoraAcademy.entity.crm.ContactSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContactSubmissionRepository extends JpaRepository<ContactSubmission, UUID> {
}
