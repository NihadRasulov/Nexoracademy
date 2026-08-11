package az.demo.NexoraAcademy.dto.catalog;

import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 80)
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "slug must be lowercase, alphanumeric, hyphen-separated")
        String slug,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 2, max = 120) String name,

        @Positive Short parentId,

        @PositiveOrZero Integer sortOrder,

        Boolean active
) {
}
