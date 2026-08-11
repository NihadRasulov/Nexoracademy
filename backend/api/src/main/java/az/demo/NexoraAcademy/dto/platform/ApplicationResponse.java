package az.demo.NexoraAcademy.dto.platform;

import java.time.Instant;

public record ApplicationResponse(
        Long id,
        Short applicationType,
        String fullname,
        String email,
        String phone,
        String letter,
        String cvFilename,
        String status,
        Instant createdAt
) {
}
