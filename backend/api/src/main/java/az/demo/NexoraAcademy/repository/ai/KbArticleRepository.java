package az.demo.NexoraAcademy.repository.ai;

import az.demo.NexoraAcademy.entity.ai.KbArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KbArticleRepository extends JpaRepository<KbArticle, UUID> {
}
