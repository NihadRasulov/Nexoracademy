package az.demo.NexoraAcademy.dto.billing;

import az.demo.NexoraAcademy.validation.DateRange;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@DateRange(startField = "validFrom", endField = "validUntil", inclusive = true)
public record ScholarshipRequest(
        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 2, max = 150) String name,

        @Size(max = 4000) String description,

        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal discountPct,

        @Positive @Max(100000) Integer maxRecipients,

        LocalDate validFrom,

        LocalDate validUntil,

        Boolean active
) {
}
