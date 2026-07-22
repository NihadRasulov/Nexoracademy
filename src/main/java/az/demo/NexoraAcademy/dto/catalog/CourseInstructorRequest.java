package az.demo.NexoraAcademy.dto.catalog;

import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CourseInstructorRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID courseId,

        @NotNull(groups = ValidationGroups.OnCreate.class) UUID instructorId,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(max = 40)
        @Pattern(regexp = "^(lead|co-instructor|mentor)$", message = "role must be one of: lead, co-instructor, mentor")
        String role
) {
}
