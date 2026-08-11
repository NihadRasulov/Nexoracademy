package az.demo.NexoraAcademy.repository.outcomes;

import az.demo.NexoraAcademy.entity.outcomes.GraduateOutcome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GraduateOutcomeRepository extends JpaRepository<GraduateOutcome, Long> {

    List<GraduateOutcome> findByCourse_Id(UUID courseId);
}
