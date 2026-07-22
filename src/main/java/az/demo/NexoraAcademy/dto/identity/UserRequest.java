package az.demo.NexoraAcademy.dto.identity;

import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UserRequest(
        @NotBlank(groups = ValidationGroups.OnCreate.class) @Email @Size(max = 255) String email,

        @Pattern(regexp = "^\\+?[0-9 ()-]{6,20}$", message = "phone must be a valid phone number")
        String phone,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 2, max = 150) String fullName,

        @NotBlank(groups = ValidationGroups.OnCreate.class)
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "password must be 8-72 characters and contain at least one letter and one digit"
        )
        String password,

        UserRole role,

        AccountStatus status,

        @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "locale must look like 'az' or 'az-AZ'")
        String locale,

        @Size(max = 50) Map<String, Object> profile
) {
}
