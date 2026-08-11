package az.demo.NexoraAcademy.dto.catalog;

import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record InstructorRequest(
        UUID userId,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 2, max = 150) String fullName,

        @Size(max = 8000) String bio,

        @Size(max = 2048)
        @Pattern(regexp = "^https?://.+", message = "photoUrl must be a valid http(s) URL")
        String photoUrl,

        @Size(max = 2048)
        @Pattern(regexp = "^https?://.+", message = "linkedinUrl must be a valid http(s) URL")
        String linkedinUrl,

        @Size(max = 50) List<Map<String, Object>> certifications,

        Boolean active
) {
}
