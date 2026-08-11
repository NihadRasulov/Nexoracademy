package az.demo.NexoraAcademy.dto.platform;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotNull Short applicationType,

        @NotBlank @Size(max = 200) String fullname,

        @NotBlank @Email @Size(max = 254) String email,

        @NotBlank @Pattern(regexp = "^[+\\d\\s()-]{7,30}$", message = "Invalid phone number") String phone,

        @NotBlank @Size(min = 50, max = 2000) String letter
) {
}
