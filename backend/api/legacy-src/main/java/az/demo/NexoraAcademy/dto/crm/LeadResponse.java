package az.demo.NexoraAcademy.dto.crm;

import az.demo.NexoraAcademy.entity.enums.LeadSource;
import az.demo.NexoraAcademy.entity.enums.LeadStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        UUID courseId,
        LeadSource source,
        LeadStatus status,
        UUID assignedTo,
        String consentVersion,
        Instant consentGivenAt,
        UUID duplicateOfLeadId,
        List<Map<String, Object>> activityLog,
        Instant createdAt,
        Instant updatedAt
) {
}
