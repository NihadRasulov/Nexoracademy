package az.demo.NexoraAcademy.dto.crm;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ChatSessionRequest(
        UUID userId,

        UUID leadId,

        @Size(max = 30) String channel,

        @Size(max = 2000) List<Map<String, Object>> messages
) {
}
