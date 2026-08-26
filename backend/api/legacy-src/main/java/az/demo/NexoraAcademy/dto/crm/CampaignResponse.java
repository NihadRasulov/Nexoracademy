package az.demo.NexoraAcademy.dto.crm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        String name,
        String bannerImageUrl,
        String ctaUrl,
        BigDecimal discountPct,
        Instant startsAt,
        Instant endsAt,
        Boolean active,
        Integer priority,
        UUID[] courseIds
) {
}
