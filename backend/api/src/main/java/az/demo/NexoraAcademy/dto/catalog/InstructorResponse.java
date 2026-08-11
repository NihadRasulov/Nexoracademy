package az.demo.NexoraAcademy.dto.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record InstructorResponse(
        UUID id,
        UUID userId,
        String fullName,
        String bio,
        String photoUrl,
        String linkedinUrl,
        BigDecimal avgRating,
        List<Map<String, Object>> certifications,
        Boolean active,
        Instant createdAt
) {
}
