package az.demo.NexoraAcademy.repository.catalog;

import az.demo.NexoraAcademy.entity.catalog.Course;
import az.demo.NexoraAcademy.entity.enums.DeliveryFormat;
import az.demo.NexoraAcademy.entity.enums.DifficultyLevel;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class CourseSpecifications {

    private CourseSpecifications() {
    }

    public static Specification<Course> search(String query, Short categoryId, DifficultyLevel difficulty,
                                                 DeliveryFormat deliveryFormat, Boolean published, Boolean active) {
        List<Specification<Course>> specs = Stream.of(
                titleOrSlugContains(query),
                categoryIdEquals(categoryId),
                difficultyEquals(difficulty),
                deliveryFormatEquals(deliveryFormat),
                publishedEquals(published),
                activeEquals(active)
        ).filter(Objects::nonNull).toList();

        return specs.isEmpty() ? Specification.unrestricted() : Specification.allOf(specs);
    }

    private static Specification<Course> titleOrSlugContains(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String like = "%" + query.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("slug")), like),
                cb.like(cb.lower(root.get("shortDescription")), like)
        );
    }

    private static Specification<Course> categoryIdEquals(Short categoryId) {
        return categoryId == null ? null : (root, cq, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<Course> difficultyEquals(DifficultyLevel difficulty) {
        return difficulty == null ? null : (root, cq, cb) -> cb.equal(root.get("difficulty"), difficulty);
    }

    private static Specification<Course> deliveryFormatEquals(DeliveryFormat deliveryFormat) {
        return deliveryFormat == null ? null : (root, cq, cb) -> cb.equal(root.get("deliveryFormat"), deliveryFormat);
    }

    private static Specification<Course> publishedEquals(Boolean published) {
        return published == null ? null : (root, cq, cb) -> cb.equal(root.get("published"), published);
    }

    private static Specification<Course> activeEquals(Boolean active) {
        return active == null ? null : (root, cq, cb) -> cb.equal(root.get("active"), active);
    }
}
