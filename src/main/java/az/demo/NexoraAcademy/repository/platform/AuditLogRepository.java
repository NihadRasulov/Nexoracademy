package az.demo.NexoraAcademy.repository.platform;

import az.demo.NexoraAcademy.entity.platform.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);
}
