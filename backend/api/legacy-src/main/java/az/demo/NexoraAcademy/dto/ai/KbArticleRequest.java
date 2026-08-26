package az.demo.NexoraAcademy.dto.ai;

import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record KbArticleRequest(
        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 30)
        @Pattern(regexp = "^[a-z_]+$", message = "sourceType must be lowercase letters/underscores (e.g. course, faq, page, policy)")
        String sourceType,

        @Size(max = 255) String sourceRefId,

        @Size(max = 250) String title,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 100000) String content,

        Boolean active
) {
}
