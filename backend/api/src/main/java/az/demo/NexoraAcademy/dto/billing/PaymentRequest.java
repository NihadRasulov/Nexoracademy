package az.demo.NexoraAcademy.dto.billing;

import az.demo.NexoraAcademy.entity.enums.PaymentMethod;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        @NotNull(groups = ValidationGroups.OnCreate.class) UUID enrollmentId,

        @NotNull(groups = ValidationGroups.OnCreate.class) PaymentMethod method,

        @NotNull(groups = ValidationGroups.OnCreate.class) @DecimalMin("0.01") @DecimalMax("1000000.00") @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code") String currency,

        @Size(max = 150) String externalTxnId,

        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 8, max = 100)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "idempotencyKey must be alphanumeric (with - or _)")
        String idempotencyKey,

        @Size(max = 60) List<Map<String, Object>> installments
) {
}
