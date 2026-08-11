package az.demo.NexoraAcademy.dto.cms;

import az.demo.NexoraAcademy.entity.enums.CmsContentType;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CmsContentRequest(
        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 160)
        @Pattern(regexp = "^[a-z0-9]+(?:[-_.][a-z0-9]+)*$", message = "key must be lowercase alphanumeric with -, _ or . separators")
        String key,

        @NotNull(groups = ValidationGroups.OnCreate.class) CmsContentType type,

        @Size(max = 250) String title,

        @Size(max = 100000) String body,

        @Size(max = 50) Map<String, Object> data,

        Boolean published,

        @PositiveOrZero Integer sortOrder
) {
}
