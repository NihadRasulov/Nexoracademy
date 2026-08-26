package az.demo.NexoraAcademy.dto.crm;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record ContactStatusRequest(
        @NotBlank @Pattern(regexp = "pending|in_progress|resolved|archived") String status
) {
}
