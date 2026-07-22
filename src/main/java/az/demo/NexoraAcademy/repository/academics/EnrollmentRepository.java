package az.demo.NexoraAcademy.repository.academics;

import az.demo.NexoraAcademy.entity.academics.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Optional<Enrollment> findByIdempotencyKey(String idempotencyKey);

    Optional<Enrollment> findByUser_IdAndGroup_Id(UUID userId, UUID groupId);

    List<Enrollment> findByUser_Id(UUID userId);

    List<Enrollment> findByGroup_Id(UUID groupId);
}
