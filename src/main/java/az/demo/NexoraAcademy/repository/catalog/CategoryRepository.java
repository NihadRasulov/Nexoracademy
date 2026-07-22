package az.demo.NexoraAcademy.repository.catalog;

import az.demo.NexoraAcademy.entity.catalog.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Short> {

    Optional<Category> findBySlug(String slug);
}
