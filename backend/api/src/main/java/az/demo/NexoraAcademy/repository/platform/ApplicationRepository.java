package az.demo.NexoraAcademy.repository.platform;

import az.demo.NexoraAcademy.entity.platform.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
