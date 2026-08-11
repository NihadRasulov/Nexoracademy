package az.demo.NexoraAcademy.dto.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PublicInstructorResponse(UUID id, String fullName, String bio, String photoUrl,
                                       String linkedinUrl, BigDecimal avgRating,
                                       List<Map<String, Object>> certifications) {
}
