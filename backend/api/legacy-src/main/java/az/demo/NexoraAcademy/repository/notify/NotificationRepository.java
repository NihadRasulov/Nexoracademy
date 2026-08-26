package az.demo.NexoraAcademy.repository.notify;

import az.demo.NexoraAcademy.entity.notify.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser_Id(UUID userId);
}
