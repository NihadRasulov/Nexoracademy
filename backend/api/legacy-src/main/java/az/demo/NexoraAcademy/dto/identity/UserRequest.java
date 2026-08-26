package az.demo.NexoraAcademy.dto.identity;

import az.demo.NexoraAcademy.entity.enums.AccountStatus;
import az.demo.NexoraAcademy.entity.enums.UserRole;
import az.demo.NexoraAcademy.validation.PersonName;
import az.demo.NexoraAcademy.validation.PhoneNumber;
import az.demo.NexoraAcademy.validation.TrimmedStringDeserializer;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UserRequest(
        @NotBlank(groups = ValidationGroups.OnCreate.class) @Email @Size(max = 254)
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String email,

        @PhoneNumber
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String phone,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @PersonName
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String firstName,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @PersonName
        @JsonDeserialize(using = TrimmedStringDeserializer.class) String lastName,

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
