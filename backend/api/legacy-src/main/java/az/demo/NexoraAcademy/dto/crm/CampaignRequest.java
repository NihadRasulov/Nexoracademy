package az.demo.NexoraAcademy.dto.crm;

import az.demo.NexoraAcademy.validation.DateRange;
import az.demo.NexoraAcademy.validation.ValidationGroups;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@DateRange(startField = "startsAt", endField = "endsAt")
public record CampaignRequest(
        @NotBlank(groups = ValidationGroups.OnCreate.class) @Size(min = 2, max = 150) String name,

        @Size(max = 2048)
        @Pattern(regexp = "^https?://.+", message = "bannerImageUrl must be a valid http(s) URL")
        String bannerImageUrl,

        @Size(max = 2048)
        @Pattern(regexp = "^https?://.+", message = "ctaUrl must be a valid http(s) URL")
        String ctaUrl,

        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal discountPct,

        @NotNull(groups = ValidationGroups.OnCreate.class) Instant startsAt,

        @NotNull(groups = ValidationGroups.OnCreate.class) Instant endsAt,

        Boolean active,

        @PositiveOrZero @Max(10000) Integer priority,

        @Size(max = 100) UUID[] courseIds
) {
}
