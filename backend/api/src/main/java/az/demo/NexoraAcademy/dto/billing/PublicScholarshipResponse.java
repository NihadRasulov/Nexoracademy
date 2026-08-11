package az.demo.NexoraAcademy.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Does not expose applicant records or internal capacity management data. */
public record PublicScholarshipResponse(
        Short id, String name, String description, BigDecimal discountPct,
        Integer maxRecipients, LocalDate validFrom, LocalDate validUntil) {
}
