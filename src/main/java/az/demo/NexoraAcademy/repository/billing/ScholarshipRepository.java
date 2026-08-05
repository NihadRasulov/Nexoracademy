package az.demo.NexoraAcademy.repository.billing;

import az.demo.NexoraAcademy.entity.billing.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ScholarshipRepository extends JpaRepository<Scholarship, Short> {
    @Query("select s from Scholarship s where s.active = true "
            + "and (s.validFrom is null or s.validFrom <= :today) "
            + "and (s.validUntil is null or s.validUntil >= :today)")
    List<Scholarship> findPublicActive(@Param("today") LocalDate today);
}
