package az.demo.NexoraAcademy.dto.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "newPassword must be 8-72 characters and contain at least one letter and one digit"
        )
        String newPassword
) {
}
