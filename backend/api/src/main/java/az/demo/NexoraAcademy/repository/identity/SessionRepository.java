package az.demo.NexoraAcademy.repository.identity;

import az.demo.NexoraAcademy.entity.identity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByTokenHash(String tokenHash);
}
