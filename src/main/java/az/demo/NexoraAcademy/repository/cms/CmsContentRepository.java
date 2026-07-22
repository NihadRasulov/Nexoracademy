package az.demo.NexoraAcademy.repository.cms;

import az.demo.NexoraAcademy.entity.cms.CmsContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CmsContentRepository extends JpaRepository<CmsContent, Long> {

    Optional<CmsContent> findByKey(String key);
}
