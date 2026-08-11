package az.demo.NexoraAcademy.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ScholarshipResponse(
        Short id,
        String name,
        String description,
        BigDecimal discountPct,
        Integer maxRecipients,
        LocalDate validFrom,
        LocalDate validUntil,
        Boolean active,
        List<Map<String, Object>> applications
) {
}
