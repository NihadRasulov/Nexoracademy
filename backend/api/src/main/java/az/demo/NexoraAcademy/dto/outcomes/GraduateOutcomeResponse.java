package az.demo.NexoraAcademy.dto.outcomes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GraduateOutcomeResponse(
        Long id,
        UUID userId,
        UUID courseId,
        String companyName,
        String jobTitle,
        LocalDate employedAt,
        String salaryBand,
        Boolean publicStory,
        String storyText,
        Instant createdAt
) {
}
