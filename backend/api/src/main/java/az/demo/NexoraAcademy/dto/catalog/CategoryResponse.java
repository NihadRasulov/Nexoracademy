package az.demo.NexoraAcademy.dto.catalog;

public record CategoryResponse(
        Short id,
        String slug,
        String name,
        Short parentId,
        Integer sortOrder,
        Boolean active
) {
}
