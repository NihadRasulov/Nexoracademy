package az.demo.NexoraAcademy.service.cms;

import az.demo.NexoraAcademy.dto.cms.CmsContentRequest;
import az.demo.NexoraAcademy.dto.cms.CmsContentResponse;
import az.demo.NexoraAcademy.entity.cms.CmsContent;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.cms.CmsContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CmsContentService {

    private final CmsContentRepository cmsContentRepository;

    @Transactional(readOnly = true)
    public List<CmsContentResponse> findAll() {
        return cmsContentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CmsContentResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public CmsContentResponse create(CmsContentRequest request) {
        assertKeyAvailable(request.key(), null);

        CmsContent content = new CmsContent();
        content.setKey(request.key());
        applyFields(content, request);

        return toResponse(cmsContentRepository.saveAndFlush(content));
    }

    public CmsContentResponse update(Long id, CmsContentRequest request) {
        CmsContent content = getOrThrow(id);
        assertKeyAvailable(request.key(), id);

        content.setKey(request.key());
        applyFields(content, request);

        return toResponse(cmsContentRepository.saveAndFlush(content));
    }

    public CmsContentResponse patch(Long id, CmsContentRequest request) {
        CmsContent content = getOrThrow(id);

        if (request.key() != null) {
            assertKeyAvailable(request.key(), id);
            content.setKey(request.key());
        }
        if (request.type() != null) content.setType(request.type());
        if (request.title() != null) content.setTitle(request.title());
        if (request.body() != null) content.setBody(request.body());
        if (request.data() != null) content.setData(request.data());
        if (request.published() != null) content.setPublished(request.published());
        if (request.sortOrder() != null) content.setSortOrder(request.sortOrder());

        return toResponse(cmsContentRepository.saveAndFlush(content));
    }

    public void delete(Long id) {
        if (!cmsContentRepository.existsById(id)) {
            throw ResourceNotFoundException.of("CmsContent", id);
        }
        cmsContentRepository.deleteById(id);
    }

    private void applyFields(CmsContent content, CmsContentRequest request) {
        content.setType(request.type());
        content.setTitle(request.title());
        content.setBody(request.body());
        content.setData(request.data() != null ? request.data() : new HashMap<>());
        content.setPublished(request.published() != null ? request.published() : false);
        content.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
    }

    private void assertKeyAvailable(String key, Long currentId) {
        cmsContentRepository.findByKey(key).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw DuplicateResourceException.of("CmsContent", "key", key);
            }
        });
    }

    private CmsContent getOrThrow(Long id) {
        return cmsContentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CmsContent", id));
    }

    private CmsContentResponse toResponse(CmsContent content) {
        return new CmsContentResponse(
                content.getId(),
                content.getKey(),
                content.getType(),
                content.getTitle(),
                content.getBody(),
                content.getData(),
                content.getPublished(),
                content.getSortOrder(),
                content.getUpdatedBy() != null ? content.getUpdatedBy().getId() : null,
                content.getUpdatedAt()
        );
    }
}
