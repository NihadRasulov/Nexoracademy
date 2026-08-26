package az.demo.NexoraAcademy.dto.academics;

import jakarta.validation.constraints.Size;

public record CancelEnrollmentRequest(
        @Size(max = 255) String reason
) {
}
