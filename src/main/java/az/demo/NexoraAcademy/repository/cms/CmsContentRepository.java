package az.demo.NexoraAcademy.repository.cms;

import az.demo.NexoraAcademy.entity.cms.CmsContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import az.demo.NexoraAcademy.entity.enums.CmsContentType;

public interface CmsContentRepository extends JpaRepository<CmsContent, Long> {

    Optional<CmsContent> findByKey(String key);

    Optional<CmsContent> findByKeyAndPublishedTrue(String key);

    List<CmsContent> findByTypeAndPublishedTrueOrderBySortOrderAscUpdatedAtDesc(CmsContentType type);
}
