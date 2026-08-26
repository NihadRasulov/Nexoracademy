package az.demo.NexoraAcademy.dto.platform;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record ApplicationStatusRequest(
        @NotBlank @Pattern(regexp = "PENDING|REVIEWED|SHORTLISTED|REJECTED") String status
) {
}
