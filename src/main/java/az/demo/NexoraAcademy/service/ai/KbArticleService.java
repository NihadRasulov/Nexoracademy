package az.demo.NexoraAcademy.service.ai;

import az.demo.NexoraAcademy.dto.ai.KbArticleRequest;
import az.demo.NexoraAcademy.dto.ai.KbArticleResponse;
import az.demo.NexoraAcademy.entity.ai.KbArticle;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.ai.KbArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class KbArticleService {

    private final KbArticleRepository kbArticleRepository;

    @Transactional(readOnly = true)
    public List<KbArticleResponse> findAll() {
        return kbArticleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public KbArticleResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public KbArticleResponse create(KbArticleRequest request) {
        KbArticle article = new KbArticle();
        applyFields(article, request);
        return toResponse(kbArticleRepository.saveAndFlush(article));
    }

    public KbArticleResponse update(UUID id, KbArticleRequest request) {
        KbArticle article = getOrThrow(id);
        applyFields(article, request);
        return toResponse(kbArticleRepository.saveAndFlush(article));
    }

    public KbArticleResponse patch(UUID id, KbArticleRequest request) {
        KbArticle article = getOrThrow(id);

        if (request.sourceType() != null) article.setSourceType(request.sourceType());
        if (request.sourceRefId() != null) article.setSourceRefId(request.sourceRefId());
        if (request.title() != null) article.setTitle(request.title());
        if (request.content() != null) article.setContent(request.content());
        if (request.active() != null) article.setActive(request.active());

        return toResponse(kbArticleRepository.saveAndFlush(article));
    }

    public void delete(UUID id) {
        if (!kbArticleRepository.existsById(id)) {
            throw ResourceNotFoundException.of("KbArticle", id);
        }
        kbArticleRepository.deleteById(id);
    }

    private void applyFields(KbArticle article, KbArticleRequest request) {
        article.setSourceType(request.sourceType());
        article.setSourceRefId(request.sourceRefId());
        article.setTitle(request.title());
        article.setContent(request.content());
        article.setActive(request.active() != null ? request.active() : true);
    }

    private KbArticle getOrThrow(UUID id) {
        return kbArticleRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("KbArticle", id));
    }

    private KbArticleResponse toResponse(KbArticle article) {
        return new KbArticleResponse(
                article.getId(),
                article.getSourceType(),
                article.getSourceRefId(),
                article.getTitle(),
                article.getContent(),
                article.getActive(),
                article.getUpdatedAt()
        );
    }
}
