package az.demo.NexoraAcademy.service.catalog;

import az.demo.NexoraAcademy.dto.catalog.CategoryRequest;
import az.demo.NexoraAcademy.dto.catalog.CategoryResponse;
import az.demo.NexoraAcademy.entity.catalog.Category;
import az.demo.NexoraAcademy.exception.DuplicateResourceException;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.catalog.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Short id) {
        return toResponse(getOrThrow(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        assertSlugAvailable(request.slug(), null);

        Category category = new Category();
        category.setSlug(request.slug());
        category.setName(request.name());
        category.setParent(resolveParent(request.parentId()));
        category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        category.setActive(request.active() != null ? request.active() : true);

        return toResponse(categoryRepository.saveAndFlush(category));
    }

    public CategoryResponse update(Short id, CategoryRequest request) {
        Category category = getOrThrow(id);
        assertSlugAvailable(request.slug(), id);

        category.setSlug(request.slug());
        category.setName(request.name());
        category.setParent(resolveParent(request.parentId()));
        category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : category.getSortOrder());
        category.setActive(request.active() != null ? request.active() : category.getActive());

        return toResponse(categoryRepository.saveAndFlush(category));
    }

    public CategoryResponse patch(Short id, CategoryRequest request) {
        Category category = getOrThrow(id);

        if (request.slug() != null) {
            assertSlugAvailable(request.slug(), id);
            category.setSlug(request.slug());
        }
        if (request.name() != null) category.setName(request.name());
        if (request.parentId() != null) category.setParent(resolveParent(request.parentId()));
        if (request.sortOrder() != null) category.setSortOrder(request.sortOrder());
        if (request.active() != null) category.setActive(request.active());

        return toResponse(categoryRepository.saveAndFlush(category));
    }

    public void delete(Short id) {
        if (!categoryRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    private void assertSlugAvailable(String slug, Short currentId) {
        categoryRepository.findBySlug(slug).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw DuplicateResourceException.of("Category", "slug", slug);
            }
        });
    }

    private Category resolveParent(Short parentId) {
        if (parentId == null) {
            return null;
        }
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", parentId));
    }

    private Category getOrThrow(Short id) {
        return categoryRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Category", id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getSlug(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getSortOrder(),
                category.getActive()
        );
    }
}
