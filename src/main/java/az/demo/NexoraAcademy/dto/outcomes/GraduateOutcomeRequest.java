package az.demo.NexoraAcademy.dto.outcomes;

import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record GraduateOutcomeRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID userId,

        @NotNull(groups = ValidationGroups.OnCreate.class) UUID courseId,

        @Size(min = 2, max = 150) String companyName,

        @Size(min = 2, max = 150) String jobTitle,

        @PastOrPresent LocalDate employedAt,

        @Size(max = 50) String salaryBand,

        Boolean publicStory,

        @Size(max = 8000) String storyText
) {
}
