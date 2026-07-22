package az.demo.NexoraAcademy.repository.catalog;

import az.demo.NexoraAcademy.entity.catalog.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstructorRepository extends JpaRepository<Instructor, UUID> {
}
